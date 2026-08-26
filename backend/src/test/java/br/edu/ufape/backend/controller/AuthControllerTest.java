package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.AuthResponse;
import br.edu.ufape.backend.dto.LoginRequest;
import br.edu.ufape.backend.dto.RegisterRequest;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Mock
    AuthService authService;
    @InjectMocks
    AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(new AuthResponse("token", Long.valueOf(1), "nome", "email", Role.ADMIN));

        ResponseEntity<AuthResponse> result = authController.register(new RegisterRequest("nome", "email", "password"));
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testLogin() {
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse("token", Long.valueOf(1), "nome", "email", Role.ADMIN));

        ResponseEntity<AuthResponse> result = authController.login(new LoginRequest("email", "password"));
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testLogout() {
        ResponseEntity<Void> result = authController.logout();
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }
}
