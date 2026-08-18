package br.edu.ufape.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.AvailabilityResponse;
import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    public ResourceService(ResourceRepository resourceRepository, ReservationRepository reservationRepository) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }

    public ResourceResponse criarRecurso(ResourceRequest request) {
        Resource resource = Resource.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .capacidade(request.getCapacidade())
                .tipo(request.getTipo())
                .statusFuncionamento(
                        request.getStatusFuncionamento() != null ? request.getStatusFuncionamento() : Boolean.TRUE)
                .build();

        resource = resourceRepository.save(resource);

        return toResponse(resource);
    }

    public List<ResourceResponse> listarRecursos() {
        return resourceRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponse> consultarDisponibilidade(AvailabilityRequest request) {
        if (request.getHorarioFim() == null || request.getHorarioInicio() == null
                || !request.getHorarioFim().isAfter(request.getHorarioInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horário de fim deve ser posterior ao horário de início");
        }

        // reserva cancelada nao conta como conflito, so pendente e confirmada bloqueiam
        List<StatusReserva> statusesAtivos = List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

        List<Long> idsOcupados = reservationRepository.findConflictingResourceIds(
                request.getData(),
                request.getHorarioInicio(),
                request.getHorarioFim(),
                statusesAtivos);

        return resourceRepository.findAll().stream()
                .map(resource -> new AvailabilityResponse(
                        resource.getId(),
                        resource.getNome(),
                        resource.getTipo(),
                        resource.getDescricao(),
                        resource.getCapacidade(),
                        !idsOcupados.contains(resource.getId())))
                .collect(Collectors.toList());
    }

    private ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getNome(),
                resource.getDescricao(),
                resource.getCapacidade(),
                resource.getTipo(),
                resource.getStatusFuncionamento());
    }
}