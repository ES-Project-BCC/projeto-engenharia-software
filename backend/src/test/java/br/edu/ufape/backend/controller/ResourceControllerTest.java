package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.AvailabilityResponse;
import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import br.edu.ufape.backend.service.ResourceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ResourceControllerTest {
    @Mock
    ResourceService resourceService;
    @InjectMocks
    ResourceController resourceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCriarRecurso() {
        when(resourceService.criarRecurso(any(ResourceRequest.class))).thenReturn(new ResourceResponse(Long.valueOf(1), "nome", "descricao", Integer.valueOf(0), TipoRecurso.LABORATORIO, Boolean.TRUE));

        ResponseEntity<ResourceResponse> result = resourceController.criarRecurso(new ResourceRequest("nome", "descricao", Integer.valueOf(0), TipoRecurso.LABORATORIO, Boolean.TRUE));
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testListarRecursos() {
        when(resourceService.listarRecursos()).thenReturn(List.of(new ResourceResponse(Long.valueOf(1), "nome", "descricao", Integer.valueOf(0), TipoRecurso.LABORATORIO, Boolean.TRUE)));

        ResponseEntity<List<ResourceResponse>> result = resourceController.listarRecursos();
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testConsultarDisponibilidade() {
        when(resourceService.consultarDisponibilidade(any(AvailabilityRequest.class))).thenReturn(List.of(new AvailabilityResponse(Long.valueOf(1), "nome", TipoRecurso.LABORATORIO, "descricao", Integer.valueOf(0), true)));

        ResponseEntity<List<AvailabilityResponse>> result = resourceController.consultarDisponibilidade(LocalDate.of(2026, Month.AUGUST, 18), LocalTime.of(21, 4, 15), LocalTime.of(21, 4, 15));
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    // ---- testes de editarRecurso (task #148) ----

    @Test
    @DisplayName("PUT /api/resources/{id} deve retornar 200 OK com o recurso atualizado")
    void testEditarRecurso_sucesso() {
        ResourceRequest request = new ResourceRequest("Lab B", "Lab atualizado", 40, TipoRecurso.LABORATORIO, false);
        ResourceResponse expected = new ResourceResponse(1L, "Lab B", "Lab atualizado", 40, TipoRecurso.LABORATORIO, false);

        when(resourceService.editarRecurso(eq(1L), any(ResourceRequest.class))).thenReturn(expected);

        ResponseEntity<ResourceResponse> result = resourceController.editarRecurso(1L, request);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(expected, result.getBody());
    }

    @Test
    @DisplayName("PUT /api/resources/{id} deve propagar 404 quando o service lança NOT_FOUND")
    void testEditarRecurso_naoEncontrado() {
        ResourceRequest request = new ResourceRequest("X", "Y", 1, TipoRecurso.EQUIPAMENTO, true);

        when(resourceService.editarRecurso(eq(999L), any(ResourceRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado com id: 999"));

        assertThatThrownBy(() -> resourceController.editarRecurso(999L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
