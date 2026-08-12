package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
            Resource resource,
            LocalDate data,
            LocalTime horarioInicio,
            LocalTime horarioFim);
}
