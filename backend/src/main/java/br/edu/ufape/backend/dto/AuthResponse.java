package br.edu.ufape.backend.dto;

import br.edu.ufape.backend.model.Role;

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
