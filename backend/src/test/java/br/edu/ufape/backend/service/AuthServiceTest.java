package br.edu.ufape.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ufape.backend.dto.LoginRequest;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.repository.UserRepository;
import br.edu.ufape.backend.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldThrowUnauthorizedOnInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        LoginRequest request = new LoginRequest("teste@email.com", "senhaerrada");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginShouldReturnTokenForValidUser() {
        User user = User.builder().id(1L).nome("Ana").email("ana@email.com").password("encoded").role(Role.USER)
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ana@email.com", Role.USER.name())).thenReturn("token-123");

        var response = authService.login(new LoginRequest("ana@email.com", "senha123"));

        assertEquals("token-123", response.getToken());
        assertEquals("ana@email.com", response.getEmail());
    }
}
