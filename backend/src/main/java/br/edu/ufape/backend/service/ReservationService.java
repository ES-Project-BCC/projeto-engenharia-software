package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
<<<<<<< HEAD
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import org.springframework.http.HttpStatus;
=======
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import br.edu.ufape.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
>>>>>>> d9e7307 (fix: vincular usuário autenticado e status inicial em ReservationService (#43))
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository, ResourceRepository resourceRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    public ReservationResponse createReservation(ReservationRequest request) {
<<<<<<< HEAD
=======
        // Obter o usuário autenticado
        User user = getAuthenticatedUser();

>>>>>>> d9e7307 (fix: vincular usuário autenticado e status inicial em ReservationService (#43))
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource não encontrado"));

        if (request.getHorarioFim() == null || request.getHorarioInicio() == null
                || !request.getHorarioFim().isAfter(request.getHorarioInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário de fim deve ser maior que horário de início");
        }

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
<<<<<<< HEAD
=======
                .user(user)
>>>>>>> d9e7307 (fix: vincular usuário autenticado e status inicial em ReservationService (#43))
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
                reservation.getHorarioFim()
        );
    }
<<<<<<< HEAD
=======

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
>>>>>>> d9e7307 (fix: vincular usuário autenticado e status inicial em ReservationService (#43))
}
