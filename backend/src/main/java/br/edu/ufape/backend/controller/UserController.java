package br.edu.ufape.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.security.UserDetailsImpl;


@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/users/me")
    public MeResponse me(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = currentUser.getUser();
        return new MeResponse(user.getId(), user.getNome(), user.getEmail(), user.getRole());
    }

    
    @GetMapping("/admin/ping")
    public String adminPing() {
        return "pong - voce e ADMIN";
    }

    public record MeResponse(Long id, String nome, String email, br.edu.ufape.backend.model.Role role) {}
}
