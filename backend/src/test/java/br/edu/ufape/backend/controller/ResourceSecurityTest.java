package br.edu.ufape.backend.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ResourceSecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String BODY_VALIDO = "{\"nome\":\"Lab Seguranca\",\"descricao\":\"Descricao do lab\",\"tipo\":\"LABORATORIO\",\"statusFuncionamento\":true}";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("USER comum nao pode editar recurso - 403 Forbidden")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void putRecurso_comRoleUser_deveRetornar403() throws Exception {
        mockMvc.perform(put("/api/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Sem autenticacao nao pode editar recurso - 401/403")
    void putRecurso_semAutenticacao_deveRetornarNaoAutorizado() throws Exception {
        mockMvc.perform(put("/api/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("ADMIN pode editar recurso - 404 pois recurso nao existe no H2 mas passou a seguranca")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void putRecurso_comRoleAdmin_devePassarSeguranca() throws Exception {
        mockMvc.perform(put("/api/resources/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Usu?rio autenticado pode buscar recurso por ID - 404 nao existe mas passou a seguranca")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getRecursoPorId_comUserAutenticado_devePassarSeguranca() throws Exception {
        mockMvc.perform(get("/api/resources/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sem autenticacao nao pode buscar recurso por ID - 401/403")
    void getRecursoPorId_semAutenticacao_deveRetornarNaoAutorizado() throws Exception {
        mockMvc.perform(get("/api/resources/1"))
                .andExpect(status().is4xxClientError());
    }
}