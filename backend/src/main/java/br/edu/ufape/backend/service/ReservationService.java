package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import br.edu.ufape.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository, ResourceRepository resourceRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    public ReservationResponse createReservation(ReservationRequest request) {
        User user = getAuthenticatedUser();

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource não encontrado"));


        // nao considera reservas canceladas como conflito (task #84)
        List<StatusReserva> statusesAtivos = List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);
        List<Long> idsConflitantes = reservationRepository.findConflictingResourceIds(
                request.getData(),
                request.getHorarioInicio(),
                request.getHorarioFim(),
                statusesAtivos);

        if (idsConflitantes.contains(resource.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário ocupado");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .data(request.getData())
                .horarioInicio(request.getHorarioInicio())
                .horarioFim(request.getHorarioFim())
                .status(StatusReserva.PENDENTE)
                .build();

        reservation = reservationRepository.save(reservation);

        return new ReservationResponse(
                reservation.getId(),
                resource.getId(),
                reservation.getData(),
                reservation.getHorarioInicio(),
                reservation.getHorarioFim(),
                reservation.getStatus());
    }

    public ReservationResponse cancelarReserva(Long id) {
        User usuarioAutenticado = getAuthenticatedUser();

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva não encontrada"));

        if (!reservation.getUser().getId().equals(usuarioAutenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para cancelar esta reserva");
        }

        if (reservation.getStatus() == StatusReserva.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta reserva já está cancelada");
        }

        if (reservation.getStatus() == StatusReserva.RECUSADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível cancelar uma reserva recusada");
        }

        LocalDateTime inicioReserva = LocalDateTime.of(reservation.getData(), reservation.getHorarioInicio());
        if (inicioReserva.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível cancelar uma reserva já iniciada ou encerrada");
        }

        reservation.setStatus(StatusReserva.CANCELADA);
        reservation = reservationRepository.save(reservation);

        return new ReservationResponse(
                reservation.getId(),
                reservation.getResource().getId(),
                reservation.getData(),
                reservation.getHorarioInicio(),
                reservation.getHorarioFim(),
                reservation.getStatus());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}