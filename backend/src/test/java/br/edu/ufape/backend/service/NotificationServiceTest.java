package br.edu.ufape.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ufape.backend.model.Notification;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.NotificationRepository;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Resource resource;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "antecedenciaMinutos", 60L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        resource = new Resource();
        resource.setId(1L);
        resource.setNome("Laboratório de Redes");

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setData(LocalDate.now());
    }

    @Test
    void testGerarNotificacoes_NaoDuplicaNotificacao() {
        // Reserva dentro da janela (começa daqui a 30 min)
        LocalTime inicio = LocalTime.now().plusMinutes(30);
        reservation.setHorarioInicio(inicio);
        reservation.setStatus(StatusReserva.CONFIRMADA);

        when(reservationRepository.findByStatusInAndData(any(), any())).thenReturn(List.of(reservation));
        when(notificationRepository.existsByReservation(reservation)).thenReturn(true); // Já existe notificação

        notificationService.gerarNotificacoesDeReservasProximas();

        // Verifica que não tentou salvar uma nova notificação
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testGerarNotificacoes_CriaSeNaoExistirEDentroDaJanela() {
        LocalTime inicio = LocalTime.now().plusMinutes(30);
        reservation.setHorarioInicio(inicio);
        reservation.setStatus(StatusReserva.PENDENTE);

        when(reservationRepository.findByStatusInAndData(any(), any())).thenReturn(List.of(reservation));
        when(notificationRepository.existsByReservation(reservation)).thenReturn(false); 

        notificationService.gerarNotificacoesDeReservasProximas();

        // Deve criar e salvar a notificação
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGerarNotificacoes_ForaDaJanelaNaoGera() {
        // Reserva fora da janela (começa daqui a 2 horas)
        LocalTime inicio = LocalTime.now().plusHours(2);
        reservation.setHorarioInicio(inicio);
        reservation.setStatus(StatusReserva.CONFIRMADA);

        when(reservationRepository.findByStatusInAndData(any(), any())).thenReturn(List.of(reservation));

        notificationService.gerarNotificacoesDeReservasProximas();

        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    // Status Cancelada e Recusada não são pegas pela query, mas se quisermos testar se a lógica previne algo
    // a query só passa PENDENTE e CONFIRMADA, logo a regra já está sendo aplicada na query e não gera.
    
    @Test
    void testMarcarComoLida_IsolamentoDeUsuario() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("outro@test.com");

        User outroUsuario = new User();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("outro@test.com");
        when(userRepository.findByEmail("outro@test.com")).thenReturn(Optional.of(outroUsuario));

        Notification notif = new Notification();
        notif.setId(1L);
        notif.setUser(user); // Pertence ao user 1
        notif.setReservation(reservation);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            notificationService.marcarComoLida(1L);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarcarComoLida_Sucesso() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        Notification notif = new Notification();
        notif.setId(1L);
        notif.setUser(user);
        notif.setReservation(reservation);
        notif.setLida(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any())).thenReturn(notif);

        notificationService.marcarComoLida(1L);

        assertTrue(notif.isLida());
        verify(notificationRepository, times(1)).save(notif);
    }
}
