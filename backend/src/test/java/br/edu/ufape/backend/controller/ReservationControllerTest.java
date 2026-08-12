package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @Test
    @WithMockUser(username = "user@ufape.edu.br")
    void deveCriarReservaComSucessoERetornarStatusCreated() throws Exception {
        LocalDate data = LocalDate.of(2026, 8, 15);
        LocalTime inicio = LocalTime.of(14, 0);
        LocalTime fim = LocalTime.of(16, 0);

        ReservationRequest request = new ReservationRequest(10L, data, inicio, fim);
        ReservationResponse response = new ReservationResponse(100L, 10L, data, inicio, fim, StatusReserva.PENDENTE);

        when(reservationService.createReservation(any(ReservationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.resourceId").value(10L))
                .andExpect(jsonPath("$.data").value("2026-08-15"))
                .andExpect(jsonPath("$.horarioInicio").value("14:00:00"))
                .andExpect(jsonPath("$.horarioFim").value("16:00:00"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @WithMockUser(username = "user@ufape.edu.br")
    void deveRetornarStatusConflictQuandoHouverConflitoDeHorario() throws Exception {
        LocalDate data = LocalDate.of(2026, 8, 15);
        LocalTime inicio = LocalTime.of(14, 0);
        LocalTime fim = LocalTime.of(16, 0);

        ReservationRequest request = new ReservationRequest(10L, data, inicio, fim);

        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Horário ocupado"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "user@ufape.edu.br")
    void deveRetornarStatusBadRequestQuandoHorariosForemInvalidos() throws Exception {
        LocalDate data = LocalDate.of(2026, 8, 15);
        LocalTime inicio = LocalTime.of(16, 0);
        LocalTime fim = LocalTime.of(14, 0);

        ReservationRequest request = new ReservationRequest(10L, data, inicio, fim);

        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário de fim deve ser maior que horário de início"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarStatusUnauthorizedQuandoUsuarioNaoEstiverAutenticado() throws Exception {
        LocalDate data = LocalDate.of(2026, 8, 15);
        LocalTime inicio = LocalTime.of(14, 0);
        LocalTime fim = LocalTime.of(16, 0);

        ReservationRequest request = new ReservationRequest(10L, data, inicio, fim);

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
