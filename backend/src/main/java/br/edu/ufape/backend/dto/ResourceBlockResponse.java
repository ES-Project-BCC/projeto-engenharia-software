package br.edu.ufape.backend.dto;

import java.time.LocalDateTime;

public class ResourceBlockResponse {

    private Long id;
    private Long resourceId;
    private String resourceNome;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private String motivo;

    public ResourceBlockResponse() {
    }

    public ResourceBlockResponse(Long id, Long resourceId, String resourceNome, LocalDateTime inicio, LocalDateTime fim,
            String motivo) {
        this.id = id;
        this.resourceId = resourceId;
        this.resourceNome = resourceNome;
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
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

    public String getResourceNome() {
        return resourceNome;
    }

    public void setResourceNome(String resourceNome) {
        this.resourceNome = resourceNome;
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