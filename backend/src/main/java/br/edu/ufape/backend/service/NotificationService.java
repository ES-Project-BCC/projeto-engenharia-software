package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.NotificationResponse;
import br.edu.ufape.backend.model.Notification;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.NotificationRepository;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {

    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Value("${notification.antecedencia-minutos:60}")
    private long antecedenciaMinutos;

    public NotificationService(ReservationRepository reservationRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // a cada 5 minutos
    public void gerarNotificacoesDeReservasProximas() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.plusMinutes(antecedenciaMinutos);
        LocalDate hoje = agora.toLocalDate();

        List<Reservation> candidatas = reservationRepository.findByStatusInAndData(
                List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA), hoje);

        for (Reservation reserva : candidatas) {
            LocalDateTime inicioReserva = reserva.getData().atTime(reserva.getHorarioInicio());
            boolean dentroDaJanela = !inicioReserva.isBefore(agora) && !inicioReserva.isAfter(limite);

            if (dentroDaJanela && !notificationRepository.existsByReservation(reserva)) {
                Notification notification = Notification.builder()
                        .user(reserva.getUser())
                        .reservation(reserva)
                        .mensagem(montarMensagem(reserva))
                        .criadaEm(agora)
                        .lida(false)
                        .build();
                notificationRepository.save(notification);
            }
        }
    }

    public Page<NotificationResponse> listarMinhasNotificacoes(Pageable pageable) {
        User user = getAuthenticatedUser();
        return notificationRepository.findByUserOrderByCriadaEmDesc(user, pageable)
                .map(this::toResponse);
    }

    public NotificationResponse marcarComoLida(Long id) {
        User user = getAuthenticatedUser();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta notificação não pertence a você");
        }

        notification.setLida(true);
        notification = notificationRepository.save(notification);

        return toResponse(notification);
    }

    public long contarNaoLidas() {
        User user = getAuthenticatedUser();
        return notificationRepository.countByUserAndLidaFalse(user);
    }

    private String montarMensagem(Reservation reserva) {
        return String.format("Sua reserva de %s começa às %s.",
                reserva.getResource().getNome(), reserva.getHorarioInicio());
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getReservation().getId(),
                notification.getMensagem(),
                notification.getCriadaEm(),
                notification.isLida());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}