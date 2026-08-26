package br.edu.ufape.backend.dto;

import br.edu.ufape.backend.model.enums.StatusReserva;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationAdminResponse {

    private Long id;
    private Long userId;
    private String userNome;
    private String userEmail;
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private StatusReserva status;
}