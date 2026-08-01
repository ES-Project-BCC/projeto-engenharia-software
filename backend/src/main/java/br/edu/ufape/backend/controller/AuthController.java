package br.edu.ufape.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufape.backend.dto.AuthResponse;
import br.edu.ufape.backend.dto.LoginRequest;
import br.edu.ufape.backend.dto.RegisterRequest;
import br.edu.ufape.backend.service.AuthService;

import jakarta.validation.Valid;

/**
 * Endpoints publicos de autenticacao: registro, login e logout.
 * Nenhum deles exige token (ver SecurityConfig: "/api/auth/**" e permitAll).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Como a autenticacao e stateless (JWT, sem sessao no servidor),
        // nao ha nada para "invalidar" no backend por padrao: o token
        // continua valido ate expirar. O logout de verdade acontece no
        // frontend, descartando o token guardado (ex.: localStorage).
        //
        // Isso satisfaz o endpoint exigido pelo enunciado. Se no futuro
        // for necessario revogar um token antes da expiracao (ex.: usuario
        // suspeito), a evolucao natural e manter uma blacklist de tokens
        // (em banco ou cache) e checa-la no JwtAuthFilter.
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}