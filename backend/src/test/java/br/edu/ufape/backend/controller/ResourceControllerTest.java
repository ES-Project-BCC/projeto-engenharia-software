package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @Test
    void deveCriarRecursoComSucessoERetornarStatusCreated() throws Exception {
        ResourceRequest request = new ResourceRequest("Sala 01", "Laboratório", 30, "LAB", true);
        ResourceResponse response = new ResourceResponse(1L, "Sala 01", "Laboratório", 30, "LAB", true);

        when(resourceService.criarRecurso(any(ResourceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Sala 01"))
                .andExpect(jsonPath("$.descricao").value("Laboratório"))
                .andExpect(jsonPath("$.capacidade").value(30))
                .andExpect(jsonPath("$.tipo").value("LAB"))
                .andExpect(jsonPath("$.statusFuncionamento").value(true));
    }

    @Test
    void deveListarRecursosComSucessoERetornarStatusOk() throws Exception {
        ResourceResponse res1 = new ResourceResponse(1L, "Sala 01", "Laboratório", 30, "LAB", true);
        ResourceResponse res2 = new ResourceResponse(2L, "Auditório", "Auditório Central", 100, "AUD", true);
        List<ResourceResponse> listaResponse = List.of(res1, res2);

        when(resourceService.listarRecursos()).thenReturn(listaResponse);

        mockMvc.perform(get("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Sala 01"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nome").value("Auditório"));
    }
}
