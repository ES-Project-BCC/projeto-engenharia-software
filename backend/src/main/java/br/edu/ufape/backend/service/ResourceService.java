package br.edu.ufape.backend.service;

import org.springframework.stereotype.Service;

import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.repository.ResourceRepository;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourceResponse criarRecurso(ResourceRequest request) {
        Resource resource = Resource.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .capacidade(request.getCapacidade())
                .tipo(request.getTipo())
                .statusFuncionamento(
                        request.getStatusFuncionamento() != null ? request.getStatusFuncionamento() : true)

                .build();

        resource = resourceRepository.save(resource);

        return toResponse(resource);
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