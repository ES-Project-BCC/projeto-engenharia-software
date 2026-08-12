package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(ReservationRepository reservationRepository, ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
    }

    public ReservationResponse createReservation(ReservationRequest request) {
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
                .resource(resource)
                .data(request.getData())
                .horarioInicio(request.getHorarioInicio())
                .horarioFim(request.getHorarioFim())
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
}
