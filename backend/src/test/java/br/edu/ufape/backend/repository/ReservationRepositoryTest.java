package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager entityManager;

    private Resource resource;
    private User user;

    @BeforeEach
    void setUp() {
        // precisei criar o user pq reservation tem user nullable=false
        user = User.builder()
                .nome("Usuario Teste")
                .email("usuario@teste.com")
                .password("senhaHash")
                .role(Role.USER)
                .build();
        entityManager.persist(user);

        // tipo tbm e obrigatorio no resource
        resource = Resource.builder()
                .nome("Laboratorio 1B")
                .descricao("Laboratorio de Programacao")
                .tipo(TipoRecurso.LABORATORIO)
                .statusFuncionamento(true)
                .build();
        entityManager.persist(resource);
    }

    @Test
    @DisplayName("deve retornar true quando existe reserva com horário conflitante")
    void shouldReturnTrueWhenReservationOverlapExists() {
        Reservation existing = Reservation.builder()
                .resource(resource)
                .user(user)
                .data(LocalDate.of(2026, 8, 12))
                .horarioInicio(LocalTime.of(10, 0))
                .horarioFim(LocalTime.of(11, 0))
                .status(StatusReserva.PENDENTE)
                .build();
        entityManager.persist(existing);
        entityManager.flush();

        // nova reserva 10:30 a 11:30 — deve conflitar com a existente 10:00-11:00
        // o servico passa (horarioFim, horarioInicio) da nova reserva pra esse metodo
        boolean conflict = reservationRepository
                .existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        resource,
                        LocalDate.of(2026, 8, 12),
                        LocalTime.of(11, 30), // fim da nova reserva
                        LocalTime.of(10, 30)  // inicio da nova reserva
                );

        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("deve retornar false quando não existe reserva com horário conflitante")
    void shouldReturnFalseWhenNoReservationOverlapExists() {
        Reservation existing = Reservation.builder()
                .resource(resource)
                .user(user)
                .data(LocalDate.of(2026, 8, 12))
                .horarioInicio(LocalTime.of(8, 0))
                .horarioFim(LocalTime.of(9, 0))
                .status(StatusReserva.PENDENTE)
                .build();
        entityManager.persist(existing);
        entityManager.flush();

        // nova reserva 9:00 a 10:00 — adjacente, nao deve conflitar
        boolean conflict = reservationRepository
                .existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        resource,
                        LocalDate.of(2026, 8, 12),
                        LocalTime.of(10, 0), // fim da nova reserva
                        LocalTime.of(9, 0)   // inicio da nova reserva
                );

        assertThat(conflict).isFalse();
    }
}
