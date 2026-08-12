package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan returns true when a reservation overlaps")
    void shouldReturnTrueWhenReservationOverlapExists() {
        Resource resource = Resource.builder()
                .nome("Laboratorio 1B")
                .descricao("Laboratorio de Programacao")
                .build();
        entityManager.persist(resource);

        Reservation existing = Reservation.builder()
                .resource(resource)
                .data(LocalDate.of(2026, 8, 12))
                .horarioInicio(LocalTime.of(10, 0))
                .horarioFim(LocalTime.of(11, 0))
                .build();
        entityManager.persist(existing);
        entityManager.flush();

        boolean conflict = reservationRepository
                .existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        resource,
                        LocalDate.of(2026, 8, 12),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 30)
                );

        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan returns false when no reservations overlap")
    void shouldReturnFalseWhenNoReservationOverlapExists() {
        Resource resource = Resource.builder()
                .nome("Laboratorio 2")
                .descricao("Laboratorio de Redes")
                .build();
        entityManager.persist(resource);

        Reservation existing = Reservation.builder()
                .resource(resource)
                .data(LocalDate.of(2026, 8, 12))
                .horarioInicio(LocalTime.of(8, 0))
                .horarioFim(LocalTime.of(9, 0))
                .build();
        entityManager.persist(existing);
        entityManager.flush();

        boolean conflict = reservationRepository
                .existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                        resource,
                        LocalDate.of(2026, 8, 12),
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
                );

        assertThat(conflict).isFalse();
    }
}
