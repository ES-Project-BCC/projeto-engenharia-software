package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.ResourceBlockRequest;
import br.edu.ufape.backend.dto.ResourceBlockResponse;
import br.edu.ufape.backend.service.ResourceBlockService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes da US12 - Bloqueio administrativo de recursos (camada de controller).
 */
class ResourceBlockControllerTest {

    @Mock
    ResourceBlockService resourceBlockService;

    @InjectMocks
    ResourceBlockController resourceBlockController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // teste 1: criar bloqueio válido deve retornar 201 CREATED
    @Test
    void testCriarBloqueio_deveRetornar201() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 1, 12, 0);

        ResourceBlockResponse mockResponse = new ResourceBlockResponse(
                1L, 10L, "Laboratório A", inicio, fim, "Manutenção preventiva");

        when(resourceBlockService.criarBloqueio(any(ResourceBlockRequest.class))).thenReturn(mockResponse);

        ResponseEntity<ResourceBlockResponse> result = resourceBlockController.criarBloqueio(
                new ResourceBlockRequest(10L, inicio, fim, "Manutenção preventiva"));

        Assertions.assertEquals(HttpStatus.CREATED, result.getStatusCode());
        Assertions.assertNotNull(result.getBody());
        Assertions.assertEquals(1L, result.getBody().getId());
    }

    @Test
    void testListarBloqueios_deveRetornarOk() {
        when(resourceBlockService.listarBloqueios(10L)).thenReturn(List.of());

        ResponseEntity<List<ResourceBlockResponse>> result = resourceBlockController.listarBloqueios(10L);

        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }
}