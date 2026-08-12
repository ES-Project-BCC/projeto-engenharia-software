package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import br.edu.ufape.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ReservationService reservationService;

    private User userMock;
    private Resource resourceMock;
    private LocalDate dataReserva;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    @BeforeEach
    void setUp() {
        userMock = new User();
        userMock.setId(1L);
        userMock.setEmail("user@ufape.edu.br");

        resourceMock = new Resource();
        resourceMock.setId(10L);

        dataReserva = LocalDate.of(2026, 8, 15);
        horaInicio = LocalTime.of(14, 0);
        horaFim = LocalTime.of(16, 0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(boolean authenticated, String email) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(authenticated);
        if (authenticated) {
            when(authentication.getName()).thenReturn(email);
        }
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void deveCriarReservaComSucesso() {
        mockAuthentication(true, "user@ufape.edu.br");
        ReservationRequest request = new ReservationRequest(10L, dataReserva, horaInicio, horaFim);

        Reservation reservationSalva = Reservation.builder()
                .id(100L)
                .user(userMock)
                .resource(resourceMock)
                .data(dataReserva)
                .horarioInicio(horaInicio)
                .horarioFim(horaFim)
                .status(StatusReserva.PENDENTE)
                .build();

        when(userRepository.findByEmail("user@ufape.edu.br")).thenReturn(Optional.of(userMock));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resourceMock));
        when(reservationRepository.existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                eq(resourceMock), eq(dataReserva), eq(horaFim), eq(horaInicio))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationSalva);

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getResourceId());
        assertEquals(dataReserva, response.getData());
        assertEquals(horaInicio, response.getHorarioInicio());
        assertEquals(horaFim, response.getHorarioFim());
        assertEquals(StatusReserva.PENDENTE, response.getStatus());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoAutenticado() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        ReservationRequest request = new ReservationRequest(10L, dataReserva, horaInicio, horaFim);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            reservationService.createReservation(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Usuário não autenticado", exception.getReason());
        verifyNoInteractions(resourceRepository, reservationRepository);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoNoBanco() {
        mockAuthentication(true, "nao_existe@ufape.edu.br");
        when(userRepository.findByEmail("nao_existe@ufape.edu.br")).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest(10L, dataReserva, horaInicio, horaFim);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            reservationService.createReservation(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuário não encontrado", exception.getReason());
        verifyNoInteractions(resourceRepository, reservationRepository);
    }

    @Test
    void deveLancarExcecaoQuandoRecursoNaoEncontrado() {
        mockAuthentication(true, "user@ufape.edu.br");
        when(userRepository.findByEmail("user@ufape.edu.br")).thenReturn(Optional.of(userMock));
        when(resourceRepository.findById(10L)).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest(10L, dataReserva, horaInicio, horaFim);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            reservationService.createReservation(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Resource não encontrado", exception.getReason());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void deveLancarExcecaoQuandoHorarioFimForIgualOuMenorQueInicio() {
        mockAuthentication(true, "user@ufape.edu.br");
        when(userRepository.findByEmail("user@ufape.edu.br")).thenReturn(Optional.of(userMock));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resourceMock));

        ReservationRequest request = new ReservationRequest(10L, dataReserva, horaFim, horaInicio);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            reservationService.createReservation(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Horário de fim deve ser maior que horário de início", exception.getReason());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void deveLancarExcecaoQuandoHorarioInicioForNulo() {
        mockAuthentication(true, "user@ufape.edu.br");
        when(userRepository.findByEmail("user@ufape.edu.br")).thenReturn(Optional.of(userMock));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resourceMock));

        ReservationRequest request = new ReservationRequest(10L, dataReserva, null, horaFim);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            reservationService.createReservation(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void deveLancarExcecaoQuandoConflitoDeHorarioExistir() {
        mockAuthentication(true, "user@ufape.edu.br");
        when(userRepository.findByEmail("user@ufape.edu.br")).thenReturn(Optional.of(userMock));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resourceMock));
        when(reservationRepository.existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                eq(resourceMock), eq(dataReserva), eq(horaFim), eq(horaInicio))).thenReturn(true);

        ReservationRequest request = new ReservationRequest(10L, dataReserva, horaInicio, horaFim);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            reservationService.createReservation(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Horário ocupado", exception.getReason());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
