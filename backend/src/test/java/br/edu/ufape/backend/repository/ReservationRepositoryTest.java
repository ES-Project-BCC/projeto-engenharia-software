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
import java.util.List;

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

    private static final List<StatusReserva> STATUS_ATIVOS =
            List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

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

    private void persistReservation(LocalDate data, LocalTime inicio, LocalTime fim, StatusReserva status) {
        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(user)
                .data(data)
                .horarioInicio(inicio)
                .horarioFim(fim)
                .status(status)
                .build();
        entityManager.persist(reservation);
        entityManager.flush();
    }

    @Test
    @DisplayName("deve retornar o id do recurso quando existe reserva com horário conflitante")
    void shouldReturnResourceIdWhenReservationOverlapExists() {
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.PENDENTE);

        // nova reserva 10:30 a 11:30 — deve conflitar com a existente 10:00-11:00
        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(10, 30),
                LocalTime.of(11, 30),
                STATUS_ATIVOS);

        assertThat(conflitantes).containsExactly(resource.getId());
    }

    @Test
    @DisplayName("não deve retornar conflito quando não existe reserva com horário conflitante")
    void shouldNotReturnConflictWhenNoReservationOverlapExists() {
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(8, 0), LocalTime.of(9, 0), StatusReserva.PENDENTE);

        // nova reserva 9:00 a 10:00 — adjacente, nao deve conflitar
        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                STATUS_ATIVOS);

        assertThat(conflitantes).isEmpty();
    }

    @Test
    @DisplayName("deve detectar conflito quando a reserva é idêntica")
    void shouldDetectConflict_whenReservationIsIdentical() {
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.PENDENTE);

        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                STATUS_ATIVOS);

        assertThat(conflitantes).containsExactly(resource.getId());
    }

    @Test
    @DisplayName("deve detectar conflito quando a nova reserva está totalmente contida na existente")
    void shouldDetectConflict_whenNewReservationIsContainedInExisting() {
        // existente: 09:00 - 12:00 (janela ampla)
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(9, 0), LocalTime.of(12, 0), StatusReserva.PENDENTE);

        // nova: 10:00 - 11:00 (totalmente dentro da existente)
        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                STATUS_ATIVOS);

        assertThat(conflitantes).containsExactly(resource.getId());
    }

    @Test
    @DisplayName("deve detectar conflito quando a nova reserva engloba totalmente a existente")
    void shouldDetectConflict_whenNewReservationEnglobesExisting() {
        // existente: 10:00 - 11:00 (janela estreita)
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.PENDENTE);

        // nova: 09:00 - 12:00 (engloba totalmente a existente)
        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                STATUS_ATIVOS);

        assertThat(conflitantes).containsExactly(resource.getId());
    }

    @Test
    @DisplayName("não deve retornar conflito quando a única reserva no período está cancelada")
    void shouldNotConflict_whenOnlyReservationIsCancelled() {
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.CANCELADA);

        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                STATUS_ATIVOS);

        assertThat(conflitantes).isEmpty();
    }

    @Test
    @DisplayName("deve detectar conflito quando a reserva está confirmada")
    void shouldDetectConflict_whenReservationIsConfirmada() {
        persistReservation(LocalDate.of(2026, 8, 12), LocalTime.of(10, 0), LocalTime.of(11, 0), StatusReserva.CONFIRMADA);

        List<Long> conflitantes = reservationRepository.findConflictingResourceIds(
                LocalDate.of(2026, 8, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                STATUS_ATIVOS);

        assertThat(conflitantes).containsExactly(resource.getId());
    }
}