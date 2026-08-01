package br.edu.ufape.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.repository.UserRepository;
import br.edu.ufape.backend.security.UserDetailsImpl;

/**
 * Ponte entre o Spring Security e o banco de dados.
 * Sempre que o Security precisa autenticar alguem (no login, ou ao
 * validar um token no JwtAuthFilter), ele chama loadUserByUsername.
 * Aqui "username" e o email, ja que e assim que identificamos o usuario.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado com o email: " + email));
        return new UserDetailsImpl(user);
    }
}