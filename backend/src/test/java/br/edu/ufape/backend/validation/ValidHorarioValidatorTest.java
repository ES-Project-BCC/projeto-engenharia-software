package br.edu.ufape.backend.validation;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.ReservationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ValidHorarioValidatorTest {

    private final ValidHorarioValidator validator = new ValidHorarioValidator();

    @Test
    @DisplayName("Objeto generico deve retornar true")
    void objetoGenerico_deveRetornarTrue() {
        assertThat(validator.isValid("obj", null)).isTrue();
    }

    @Test
    @DisplayName("ReservationRequest com horario valido deve retornar true")
    void reservationRequest_horarioValido() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertThat(validator.isValid(request, null)).isTrue();
    }

    @Test
    @DisplayName("ReservationRequest com fim antes do inicio deve retornar false")
    void reservationRequest_fimAntesInicio() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDate.of(2026, 9, 1),
                LocalTime.of(14, 0),
                LocalTime.of(13, 0));

        assertThat(validator.isValid(request, null)).isFalse();
    }

    @Test
    @DisplayName("ReservationRequest com horario nulo deve retornar false")
    void reservationRequest_horarioNulo() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDate.of(2026, 9, 1),
                null,
                LocalTime.of(11, 0));

        assertThat(validator.isValid(request, null)).isFalse();
    }

    @Test
    @DisplayName("AvailabilityRequest com horario valido deve retornar true")
    void availabilityRequest_horarioValido() {
        AvailabilityRequest request = new AvailabilityRequest(
                LocalDate.of(2026, 9, 1),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0));

        assertThat(validator.isValid(request, null)).isTrue();
    }

    @Test
    @DisplayName("AvailabilityRequest com fim igual ao inicio deve retornar false")
    void availabilityRequest_fimIgualInicio() {
        AvailabilityRequest request = new AvailabilityRequest(
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 0));

        assertThat(validator.isValid(request, null)).isFalse();
    }

    @Test
    @DisplayName("AvailabilityRequest com horario nulo deve retornar false")
    void availabilityRequest_horarioNulo() {
        AvailabilityRequest request = new AvailabilityRequest(
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                null);

        assertThat(validator.isValid(request, null)).isFalse();
    }
}
