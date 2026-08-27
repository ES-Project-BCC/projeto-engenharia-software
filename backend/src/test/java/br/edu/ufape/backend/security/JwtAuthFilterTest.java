package br.edu.ufape.backend.security;

import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sem header Authorization deve seguir a cadeia sem autenticar")
    void semHeader_deveSeguirCadeia() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    @DisplayName("Header sem prefixo Bearer deve seguir a cadeia sem autenticar")
    void headerSemBearer_deveSeguirCadeia() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    @DisplayName("Token valido deve autenticar o usuario no SecurityContext")
    void tokenValido_deveAutenticar() throws Exception {
        String token = "token.valido.jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenReturn("joao@ufape.br");

        User user = User.builder()
                .id(1L)
                .nome("Joao")
                .email("joao@ufape.br")
                .password("encoded")
                .role(Role.USER)
                .build();
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(userDetailsService.loadUserByUsername("joao@ufape.br")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(token, "joao@ufape.br")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("joao@ufape.br");
    }

    @Test
    @DisplayName("Token invalido deve limpar o contexto e seguir a cadeia")
    void tokenInvalido_deveLimparContexto() throws Exception {
        String token = "token.invalido";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenThrow(new RuntimeException("token invalido"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Token valido mas isTokenValid false nao deve autenticar")
    void tokenValidoMasIsTokenValidFalse_naoDeveAutenticar() throws Exception {
        String token = "token.quase.valido";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractEmail(token)).thenReturn("joao@ufape.br");

        User user = User.builder()
                .id(1L)
                .nome("Joao")
                .email("joao@ufape.br")
                .password("encoded")
                .role(Role.USER)
                .build();
        when(userDetailsService.loadUserByUsername("joao@ufape.br")).thenReturn(new UserDetailsImpl(user));
        when(jwtUtil.isTokenValid(token, "joao@ufape.br")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
