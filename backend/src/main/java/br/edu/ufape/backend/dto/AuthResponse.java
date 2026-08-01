package br.edu.ufape.backend.dto;

import br.edu.ufape.backend.model.Role;

/**
 * Retornado no login: o token JWT que o front vai guardar e mandar
 * no header Authorization das próximas requisições, mais alguns
 * dados básicos pra a UI já saber quem é o usuário sem decodificar o token.
 */
public class AuthResponse {

    private String token;
    private Long id;
    private String nome;
    private String email;
    private Role role;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String nome, String email, Role role) {
        this.token = token;
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}