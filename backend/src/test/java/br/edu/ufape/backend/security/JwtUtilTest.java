package br.edu.ufape.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // mesma chave do application-test.properties (texto puro, nao base64)
    private static final String SECRET = "chave-de-teste-fake-so-para-os-testes-automatizados-nao-usar-em-producao";
    private static final long EXPIRATION_MS = 86_400_000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);
    }

    @Test
    @DisplayName("Deve gerar token e extrair email e role corretamente")
    void deveGerarTokenEExtrairClaims() {
        String token = jwtUtil.generateToken("joao@ufape.br", "USER");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("joao@ufape.br");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("Token valido com email correto deve retornar true")
    void tokenValido_deveRetornarTrue() {
        String token = jwtUtil.generateToken("ana@ufape.br", "ADMIN");

        assertThat(jwtUtil.isTokenValid(token, "ana@ufape.br")).isTrue();
    }

    @Test
    @DisplayName("Token com email diferente deve retornar false")
    void tokenComEmailDiferente_deveRetornarFalse() {
        String token = jwtUtil.generateToken("ana@ufape.br", "USER");

        assertThat(jwtUtil.isTokenValid(token, "outro@ufape.br")).isFalse();
    }

    @Test
    @DisplayName("Token expirado deve retornar false")
    void tokenExpirado_deveRetornarFalse() {
        // expiration negativa = ja nasce expirado
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String token = jwtUtil.generateToken("exp@ufape.br", "USER");

        assertThat(jwtUtil.isTokenValid(token, "exp@ufape.br")).isFalse();
    }

    @Test
    @DisplayName("Token malformado deve retornar false sem lancar excecao")
    void tokenMalformado_deveRetornarFalse() {
        assertThat(jwtUtil.isTokenValid("token.invalido.xyz", "qualquer@email.com")).isFalse();
    }

    @Test
    @DisplayName("Deve extrair expiration no futuro")
    void deveExtrairExpirationNoFuturo() {
        String token = jwtUtil.generateToken("user@ufape.br", "USER");
        Instant expiration = jwtUtil.extractExpiration(token);

        assertThat(expiration).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Deve funcionar com secret em Base64 valido")
    void deveFuncionarComSecretBase64() {
        // 32 bytes em base64 (minimo para HS256)
        String base64Secret = java.util.Base64.getEncoder()
                .encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secret", base64Secret);

        String token = jwtUtil.generateToken("base64@ufape.br", "USER");
        assertThat(jwtUtil.isTokenValid(token, "base64@ufape.br")).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("base64@ufape.br");
    }
}
