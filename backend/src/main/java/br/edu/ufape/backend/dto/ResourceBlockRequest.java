package br.edu.ufape.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ResourceBlockRequest {

    @NotNull(message = "O recurso é obrigatório")
    private Long resourceId;

    @NotNull(message = "A data/hora de início é obrigatória")
    private LocalDateTime inicio;

    @NotNull(message = "A data/hora de fim é obrigatória")
    private LocalDateTime fim;

    @NotBlank(message = "O motivo é obrigatório")
    @Size(max = 300, message = "O motivo deve ter no máximo 300 caracteres")
    private String motivo;

    public ResourceBlockRequest() {
    }

    public ResourceBlockRequest(Long resourceId, LocalDateTime inicio, LocalDateTime fim, String motivo) {
        this.resourceId = resourceId;
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}