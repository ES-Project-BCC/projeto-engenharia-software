package br.edu.ufape.backend.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long reservationId;
    private String mensagem;
    private LocalDateTime criadaEm;
    private boolean lida;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, Long reservationId, String mensagem, LocalDateTime criadaEm, boolean lida) {
        this.id = id;
        this.reservationId = reservationId;
        this.mensagem = mensagem;
        this.criadaEm = criadaEm;
        this.lida = lida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }
}