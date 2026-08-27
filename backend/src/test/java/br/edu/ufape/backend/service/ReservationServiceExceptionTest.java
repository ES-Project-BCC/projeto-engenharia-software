package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceBlockRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import br.edu.ufape.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Testes focados nas branches de excecao do ReservationService
 * (404, 409, 403, 401) para subir cobertura de branches.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceExceptionTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceBlockRepository resourceBlockRepository;

    @InjectMocks
    private ReservationService reservationService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    private User usuarioLogado;
    private Resource resource;

    @BeforeEach
    void setUp() {
        usuarioLogado = User.builder()
                .id(1L)
                .nome("Joao Teste")
                .email("joao@ufape.br")
                .role(Role.USER)
                .build();

        resource = Resource.builder()
                .id(10L)
                .nome("Laboratorio A")
                .tipo(TipoRecurso.LABORATORIO)
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("joao@ufape.br");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        when(userRepository.findByEmail("joao@ufape.br")).thenReturn(Optional.of(usuarioLogado));
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    @DisplayName("Deve lancar 404 ao criar reserva com resource inexistente")
    void criarReserva_resourceInexistente_deveLancar404() {
        ReservationRequest request = new ReservationRequest(
                999L,
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar 409 CONFLICT quando horario ja esta ocupado por outra reserva")
    void criarReserva_horarioOcupado_deveLancar409() {
        ReservationRequest request = new ReservationRequest(
                resource.getId(),
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        when(resourceRepository.findById(resource.getId())).thenReturn(Optional.of(resource));
        when(reservationRepository.findConflictingResourceIds(any(), any(), any(), anyList()))
                .thenReturn(List.of(resource.getId()));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).contains("ocupado");
                });

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar 404 ao cancelar reserva inexistente")
    void cancelarReserva_inexistente_deveLancar404() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelarReserva(999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Deve lancar 409 ao tentar cancelar reserva RECUSADA")
    void cancelarReserva_recusada_deveLancar409() {
        Reservation reserva = Reservation.builder()
                .id(50L)
                .user(usuarioLogado)
                .resource(resource)
                .data(LocalDate.now().plusDays(2))
                .horarioInicio(LocalTime.of(10, 0))
                .horarioFim(LocalTime.of(11, 0))
                .status(StatusReserva.RECUSADA)
                .build();

        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservationService.cancelarReserva(50L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).containsIgnoringCase("recusada");
                });
    }

    @Test
    @DisplayName("Deve lancar 401 quando nao ha autenticacao")
    void getAuthenticatedUser_semAuth_deveLancar401() {
        // sobrescreve o mock do setUp para simular ausencia de autenticacao
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        ReservationRequest request = new ReservationRequest(
                resource.getId(),
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("Deve lancar 404 quando email autenticado nao existe no banco")
    void getAuthenticatedUser_usuarioNaoEncontrado_deveLancar404() {
        when(userRepository.findByEmail("joao@ufape.br")).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest(
                resource.getId(),
                LocalDate.of(2026, 9, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
