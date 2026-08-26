package br.edu.ufape.backend.dto;

import br.edu.ufape.backend.validation.ValidHorario;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@ValidHorario
public class ReservationRequest {

    @NotNull(message = "O id do recurso é obrigatório")
    private Long resourceId;

    @NotNull(message = "A data é obrigatória")
    private LocalDate data;

    @NotNull(message = "O horário de início é obrigatório")
    private LocalTime horarioInicio;

    @NotNull(message = "O horário de fim é obrigatório")
    private LocalTime horarioFim;

    public ReservationRequest() {
    }

    public ReservationRequest(Long resourceId, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim) {
        this.resourceId = resourceId;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }
}