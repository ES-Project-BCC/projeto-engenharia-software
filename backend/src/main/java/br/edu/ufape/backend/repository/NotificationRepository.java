package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Notification;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByReservation(Reservation reservation);
    Page<Notification> findByUserOrderByCriadaEmDesc(User user, Pageable pageable);
    long countByUserAndLidaFalse(User user);
}