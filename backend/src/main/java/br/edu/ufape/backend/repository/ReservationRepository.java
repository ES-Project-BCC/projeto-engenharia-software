package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.repository.projection.ResourceUsageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

        @Query("SELECT r.resource.id FROM Reservation r " +
                        "WHERE r.data = :data " +
                        "AND r.horarioInicio < :horarioFim " +
                        "AND r.horarioFim > :horarioInicio " +
                        "AND r.status IN :statusesAtivos")
        List<Long> findConflictingResourceIds(
                        @Param("data") LocalDate data,
                        @Param("horarioInicio") LocalTime horarioInicio,
                        @Param("horarioFim") LocalTime horarioFim,
                        @Param("statusesAtivos") List<StatusReserva> statusesAtivos);

        Page<Reservation> findByUserOrderByDataDescHorarioInicioDesc(User user, Pageable pageable);

        Page<Reservation> findByResourceOrderByDataDescHorarioInicioDesc(Resource resource, Pageable pageable);

        List<Reservation> findByStatusInAndData(List<StatusReserva> statuses, LocalDate data);

        @Query("SELECT r.resource.id AS resourceId, r.resource.nome AS resourceNome, COUNT(r) AS totalReservas " +
               "FROM Reservation r " +
               "WHERE r.data BETWEEN :dataInicio AND :dataFim " +
               "AND r.status <> br.edu.ufape.backend.model.enums.StatusReserva.CANCELADA " +
               "AND r.status <> br.edu.ufape.backend.model.enums.StatusReserva.RECUSADA " +
               "GROUP BY r.resource.id, r.resource.nome " +
               "ORDER BY totalReservas DESC")
        List<ResourceUsageProjection> countReservationsByResource(
                @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

        List<Reservation> findByDataBetweenAndStatusNotIn(LocalDate dataInicio, LocalDate dataFim, List<StatusReserva> statuses);

}