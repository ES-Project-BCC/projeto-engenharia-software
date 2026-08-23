package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ResourceBlockRequest;
import br.edu.ufape.backend.dto.ResourceBlockResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.ResourceBlock;
import br.edu.ufape.backend.repository.ResourceBlockRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResourceBlockService {

    private final ResourceBlockRepository resourceBlockRepository;
    private final ResourceRepository resourceRepository;

    public ResourceBlockService(ResourceBlockRepository resourceBlockRepository,
            ResourceRepository resourceRepository) {
        this.resourceBlockRepository = resourceBlockRepository;
        this.resourceRepository = resourceRepository;
    }

    public ResourceBlockResponse criarBloqueio(ResourceBlockRequest request) {
        if (!request.getFim().isAfter(request.getInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data/hora de fim deve ser maior que a de início");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource não encontrado"));

        ResourceBlock block = ResourceBlock.builder()
                .resource(resource)
                .inicio(request.getInicio())
                .fim(request.getFim())
                .motivo(request.getMotivo())
                .build();

        block = resourceBlockRepository.save(block);

        return toResponse(block);
    }

    public List<ResourceBlockResponse> listarBloqueios(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource não encontrado"));

        return resourceBlockRepository.findByResourceOrderByInicioDesc(resource)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void removerBloqueio(Long id) {
        ResourceBlock block = resourceBlockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bloqueio não encontrado"));

        resourceBlockRepository.delete(block);
    }

    private ResourceBlockResponse toResponse(ResourceBlock block) {
        return new ResourceBlockResponse(
                block.getId(),
                block.getResource().getId(),
                block.getResource().getNome(),
                block.getInicio(),
                block.getFim(),
                block.getMotivo());
    }
}