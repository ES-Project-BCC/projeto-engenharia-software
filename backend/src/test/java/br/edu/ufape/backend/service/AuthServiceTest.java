package br.edu.ufape.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
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

import br.edu.ufape.backend.dto.AuthResponse;
import br.edu.ufape.backend.dto.LoginRequest;
import br.edu.ufape.backend.dto.RegisterRequest;
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
    @DisplayName("Login com credenciais invalidas deve lancar 401")
    void loginShouldThrowUnauthorizedOnInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        LoginRequest request = new LoginRequest("teste@email.com", "senhaerrada");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Login valido deve retornar token")
    void loginShouldReturnTokenForValidUser() {
        User user = User.builder().id(1L).nome("Ana").email("ana@email.com").password("encoded").role(Role.USER)
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ana@email.com", Role.USER.name())).thenReturn("token-123");

        AuthResponse response = authService.login(new LoginRequest("ana@email.com", "senha123"));

        assertThat(response.getToken()).isEqualTo("token-123");
        assertThat(response.getEmail()).isEqualTo("ana@email.com");
    }

    @Test
    @DisplayName("Login autenticado mas usuario nao encontrado no banco deve lancar 401")
    void login_usuarioNaoEncontradoAposAuth_deveLancar401() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("fantasma@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("fantasma@email.com", "senha")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Register com email ja existente deve lancar 409 CONFLICT")
    void register_emailExistente_deveLancar409() {
        RegisterRequest request = new RegisterRequest("Joao", "joao@ufape.br", "senha123");
        when(userRepository.existsByEmail("joao@ufape.br")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).containsIgnoringCase("email");
                });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register com dados validos deve criar usuario USER e retornar token")
    void register_valido_deveCriarUsuarioERetornarToken() {
        RegisterRequest request = new RegisterRequest("Maria", "maria@ufape.br", "senha123");

        when(userRepository.existsByEmail("maria@ufape.br")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtUtil.generateToken("maria@ufape.br", Role.USER.name())).thenReturn("token-novo");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token-novo");
        assertThat(response.getEmail()).isEqualTo("maria@ufape.br");
        assertThat(response.getNome()).isEqualTo("Maria");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getId()).isEqualTo(10L);

        verify(userRepository).save(any(User.class));
    }
}
