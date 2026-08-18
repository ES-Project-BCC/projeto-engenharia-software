package br.edu.ufape.backend.dto;

import br.edu.ufape.backend.model.enums.StatusReserva;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationResponse {

    private Long id;
    private Long resourceId;
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private StatusReserva status;

    public ReservationResponse() {
    }

    public ReservationResponse(Long id, Long resourceId, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim,
            StatusReserva status) {
        this.id = id;
        this.resourceId = resourceId;
        this.data = data;
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }
}