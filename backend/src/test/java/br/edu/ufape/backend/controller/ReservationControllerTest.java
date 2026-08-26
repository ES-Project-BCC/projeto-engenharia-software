package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.MinhaReservaResponse;
import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.service.ReservationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

import static org.mockito.Mockito.*;

class ReservationControllerTest {
    @Mock
    ReservationService reservationService;
    @InjectMocks
    ReservationController reservationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReservation() {
        when(reservationService.createReservation(any(ReservationRequest.class))).thenReturn(new ReservationResponse(Long.valueOf(1), Long.valueOf(1), LocalDate.of(2026, Month.AUGUST, 18), LocalTime.of(21, 3, 57), LocalTime.of(21, 3, 57), StatusReserva.PENDENTE));

        ResponseEntity<ReservationResponse> result = reservationController.createReservation(new ReservationRequest(Long.valueOf(1), LocalDate.of(2026, Month.AUGUST, 18), LocalTime.of(21, 3, 57), LocalTime.of(21, 3, 57)));
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testListarMinhasReservas() {
        when(reservationService.listarMinhasReservas(any(Pageable.class))).thenReturn(null);

        ResponseEntity<Page<MinhaReservaResponse>> result = reservationController.listarMinhasReservas(null);
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }
}
