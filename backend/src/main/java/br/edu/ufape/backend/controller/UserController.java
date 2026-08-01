package br.edu.ufape.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.security.UserDetailsImpl;

/**
 * Endpoints de exemplo para validar, na pratica, que a autenticacao e a
 * autorizacao por role estao funcionando de ponta a ponta antes de integrar
 * com o frontend. Nao fazem parte da historia de cadastro em si, mas
 * ajudam a testar o resultado dela.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    /**
     * Qualquer usuario autenticado (ADMIN ou USER) pode acessar.
     * Sem token valido no header Authorization -> 401/403 automatico,
     * pois "/api/**" cai na regra anyRequest().authenticated() do SecurityConfig.
     */
    @GetMapping("/users/me")
    public MeResponse me(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = currentUser.getUser();
        return new MeResponse(user.getId(), user.getNome(), user.getEmail(), user.getRole());
    }

    /**
     * So usuarios com role ADMIN conseguem acessar - protegido pela regra
     * "/api/admin/**".hasRole("ADMIN") no SecurityConfig. Um usuario USER
     * autenticado recebe 403 Forbidden aqui.
     */
    @GetMapping("/admin/ping")
    public String adminPing() {
        return "pong - voce e ADMIN";
    }

    /**
     * DTO minimo so para essa resposta - de proposito NAO inclui a senha
     * (nem o hash dela), diferente de expor UserDetailsImpl diretamente,
     * que vazaria o hash da senha via getPassword() no JSON.
     */
    public record MeResponse(Long id, String nome, String email, br.edu.ufape.backend.model.Role role) {}
}