package br.edu.ufape.backend.dto;

import br.edu.ufape.backend.model.enums.TipoRecurso;

public class AvailabilityResponse {

    private Long id;
    private String nome;
    private TipoRecurso tipo;
    private String descricao;
    private Integer capacidade;
    private boolean disponivel;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(Long id, String nome, TipoRecurso tipo, String descricao,
                                Integer capacidade, boolean disponivel) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.capacidade = capacidade;
        this.disponivel = disponivel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public void setTipo(TipoRecurso tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
}