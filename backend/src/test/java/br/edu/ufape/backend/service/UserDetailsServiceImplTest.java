package br.edu.ufape.backend.service;

import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserDetailsServiceImpl userDetailsServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername() {
        // Instancia o usuário que o mock deve retornar
        User usuarioFicticio = new User(Long.valueOf(1), "nome", "email", "password", Role.ADMIN);

        // CORRIGIDO: Retorna o Optional.of() contendo o usuário em vez de null
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(usuarioFicticio));

        UserDetails result = userDetailsServiceImpl.loadUserByUsername("email");

        // CORRIGIDO: Valida os atributos essenciais para evitar erros de comparação de
        // memória
        Assertions.assertNotNull(result);
        Assertions.assertEquals("email", result.getUsername());
        Assertions.assertEquals("password", result.getPassword());
    }

}