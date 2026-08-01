package br.edu.ufape.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.edu.ufape.backend.service.UserDetailsServiceImpl;

/**
 * Configuracao central do Spring Security: define quais rotas sao publicas,
 * quais exigem autenticacao, quais exigem uma role especifica, e registra
 * o JwtAuthFilter para rodar antes do filtro padrao de autenticacao.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless com JWT nao precisa de protecao CSRF (essa protecao
                // e para autenticacao baseada em cookie/sessao).
                .csrf(csrf -> csrf.disable())

                // Necessario para o console do H2 funcionar (ele usa <frame>).
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Sem sessao HTTP: cada requisicao se autentica sozinha via token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Rotas publicas: registro, login e o console do H2 (so para dev)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Exemplo de rota restrita por role - ajustem/adicionem
                        // conforme as historias de admin forem implementadas.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Qualquer outra rota exige apenas estar autenticado
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                // Nosso filtro roda ANTES do filtro padrao de usuario/senha,
                // pois a autenticacao aqui acontece via token, nao via formulario.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}