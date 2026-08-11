package br.edu.ufape.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.repository.UserRepository;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	// cria um usuario ADMIN de fabrica quando a aplicacao sobe, se ele ainda nao
	// existir
	@Bean
	CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("admin@ufape.br")) {
				User admin = User.builder()
						.nome("Administrador")
						.email("admin@ufape.br")
						.password(passwordEncoder.encode("admin123"))
						.role(Role.ADMIN)
						.build();
				userRepository.save(admin);
			}
		};
	}

}
