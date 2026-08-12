package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import br.edu.ufape.backend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(ReservationRepository reservationRepository, ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
    }

    public ReservationResponse createReservation(ReservationRequest request) {
        validateRequest(request);

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource não encontrado"));

        User user = getAuthenticatedUser();

        boolean conflict = reservationRepository.existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                resource,
                request.getData(),
                request.getHorarioInicio(),
                request.getHorarioFim()
        );

        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário ocupado");
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(user)
                .data(request.getData())
                .horarioInicio(request.getHorarioInicio())
                .horarioFim(request.getHorarioFim())
                .build();

        reservation = reservationRepository.save(reservation);

        return new ReservationResponse(
                reservation.getId(),
                resource.getId(),
                reservation.getUser().getId(),
                reservation.getData(),
                reservation.getHorarioInicio(),
                reservation.getHorarioFim()
        );
    }

    private void validateRequest(ReservationRequest request) {
        if (request.getResourceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id do recurso é obrigatório");
        }
        if (request.getData() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data é obrigatória");
        }
        if (request.getHorarioInicio() == null || request.getHorarioFim() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os horários de início e fim são obrigatórios");
        }
        if (!request.getHorarioFim().isAfter(request.getHorarioInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário de fim deve ser maior que horário de início");
        }
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUser();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
    }
}
