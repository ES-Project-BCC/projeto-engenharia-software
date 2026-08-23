package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.ResourceBlockRequest;
import br.edu.ufape.backend.dto.ResourceBlockResponse;
import br.edu.ufape.backend.service.ResourceBlockService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resource-blocks")
public class ResourceBlockController {

    private final ResourceBlockService resourceBlockService;

    public ResourceBlockController(ResourceBlockService resourceBlockService) {
        this.resourceBlockService = resourceBlockService;
    }

    @PostMapping
    public ResponseEntity<ResourceBlockResponse> criarBloqueio(@Valid @RequestBody ResourceBlockRequest request) {
        ResourceBlockResponse response = resourceBlockService.criarBloqueio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResourceBlockResponse>> listarBloqueios(@RequestParam Long resourceId) {
        List<ResourceBlockResponse> response = resourceBlockService.listarBloqueios(resourceId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerBloqueio(@PathVariable Long id) {
        resourceBlockService.removerBloqueio(id);
        return ResponseEntity.noContent().build();
    }
}