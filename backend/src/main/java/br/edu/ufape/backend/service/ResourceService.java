package br.edu.ufape.backend.service;

import java.util.List;

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
import br.edu.ufape.backend.repository.ResourceBlockRepository;
import br.edu.ufape.backend.repository.ResourceRepository;

import java.time.LocalDateTime;

@Service
public class ResourceService {

        private final ResourceRepository resourceRepository;
        private final ReservationRepository reservationRepository;
        private final ResourceBlockRepository resourceBlockRepository;

        public ResourceService(ResourceRepository resourceRepository, ReservationRepository reservationRepository,
                        ResourceBlockRepository resourceBlockRepository) {
                this.resourceRepository = resourceRepository;
                this.reservationRepository = reservationRepository;
                this.resourceBlockRepository = resourceBlockRepository;
        }

        public ResourceResponse criarRecurso(ResourceRequest request) {
                Resource resource = Resource.builder()
                                .nome(request.getNome())
                                .descricao(request.getDescricao())
                                .capacidade(request.getCapacidade())
                                .tipo(request.getTipo())
                                .statusFuncionamento(
                                                request.getStatusFuncionamento() != null
                                                                ? request.getStatusFuncionamento()
                                                                : Boolean.TRUE)
                                .build();

                resource = resourceRepository.save(resource);

                return toResponse(resource);
        }

        // edita os campos do recurso existente, se nao achar o id da 404
        public ResourceResponse editarRecurso(Long id, ResourceRequest request) {
                Resource resource = resourceRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Recurso não encontrado com id: " + id));

                resource.setNome(request.getNome());
                resource.setDescricao(request.getDescricao());
                resource.setCapacidade(request.getCapacidade());
                resource.setTipo(request.getTipo());
                // so atualiza o status se vier preenchido, senao mantém o que tava antes
                if (request.getStatusFuncionamento() != null) {
                        resource.setStatusFuncionamento(request.getStatusFuncionamento());
                }

                resource = resourceRepository.save(resource);
                return toResponse(resource);
        }

        // busca um recurso pelo id, usado pra preencher o form de edição no front
        public ResourceResponse buscarPorId(Long id) {
                Resource resource = resourceRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Recurso não encontrado com id: " + id));
                return toResponse(resource);
        }

        public List<ResourceResponse> listarRecursos() {
                return resourceRepository.findAll().stream()
                                .map(this::toResponse)
                                .toList();
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

                // integra bloqueios administrativos: recurso bloqueado aparece como indisponivel (#171)
                LocalDateTime inicioDateTime = request.getData().atTime(request.getHorarioInicio());
                LocalDateTime fimDateTime = request.getData().atTime(request.getHorarioFim());
                List<Long> idsBloqueados = resourceBlockRepository.findBlockedResourceIds(inicioDateTime, fimDateTime);

                return resourceRepository.findAll().stream()
                                .map(resource -> new AvailabilityResponse(
                                                resource.getId(),
                                                resource.getNome(),
                                                resource.getTipo(),
                                                resource.getDescricao(),
                                                resource.getCapacidade(),
                                                !idsOcupados.contains(resource.getId())
                                                                && !idsBloqueados.contains(resource.getId())))
                                .toList();
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
