package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.MinhaReservaResponse;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.model.enums.Role;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import br.edu.ufape.backend.repository.ReservationRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

        @Mock
        private ReservationRepository reservationRepository;

        @Mock
        private ResourceRepository resourceRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private ReservationService reservationService;

        private MockedStatic<SecurityContextHolder> securityContextHolderMock;

        private User usuarioLogado;
        private User outroUsuario;
        private Resource resource;

        @BeforeEach
        void setUp() {
                usuarioLogado = User.builder()
                                .id(1L)
                                .nome("Joao Teste")
                                .email("joao@ufape.br")
                                .role(Role.USER)
                                .build();

                outroUsuario = User.builder()
                                .id(2L)
                                .nome("Maria Teste")
                                .email("maria@ufape.br")
                                .role(Role.USER)
                                .build();

                resource = Resource.builder()
                                .id(10L)
                                .nome("Laboratório A")
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
        @DisplayName("Deve listar apenas as reservas do usuário autenticado, já traduzidas")
        void deveListarReservasDoUsuarioLogado() {
                Reservation reserva = Reservation.builder()
                                .id(100L)
                                .user(usuarioLogado)
                                .resource(resource)
                                .data(LocalDate.of(2026, 9, 1))
                                .horarioInicio(LocalTime.of(10, 0))
                                .horarioFim(LocalTime.of(11, 0))
                                .status(StatusReserva.PENDENTE)
                                .build();

                Pageable pageable = PageRequest.of(0, 10);
                Page<Reservation> paginaMock = new PageImpl<>(List.of(reserva), pageable, 1);

                when(reservationRepository.findByUserOrderByDataDescHorarioInicioDesc(usuarioLogado, pageable))
                                .thenReturn(paginaMock);

                Page<MinhaReservaResponse> resultado = reservationService.listarMinhasReservas(pageable);

                assertThat(resultado.getTotalElements()).isEqualTo(1);
                MinhaReservaResponse item = resultado.getContent().get(0);
                assertThat(item.getId()).isEqualTo(100L);
                assertThat(item.getResourceNome()).isEqualTo("Laboratório A");
                assertThat(item.getStatus()).isEqualTo(StatusReserva.PENDENTE);

                verify(reservationRepository).findByUserOrderByDataDescHorarioInicioDesc(usuarioLogado, pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando o usuário não tem nenhuma reserva")
        void deveRetornarPaginaVazia_quandoSemReservas() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Reservation> paginaVazia = new PageImpl<>(List.of(), pageable, 0);

                when(reservationRepository.findByUserOrderByDataDescHorarioInicioDesc(usuarioLogado, pageable))
                                .thenReturn(paginaVazia);

                Page<MinhaReservaResponse> resultado = reservationService.listarMinhasReservas(pageable);

                assertThat(resultado.getTotalElements()).isZero();
                assertThat(resultado.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Deve cancelar reserva própria futura com sucesso")
        void deveCancelarReservaPropriaFutura() {
                Reservation reserva = Reservation.builder()
                                .id(200L)
                                .user(usuarioLogado)
                                .resource(resource)
                                .data(LocalDate.now().plusDays(1))
                                .horarioInicio(LocalTime.of(10, 0))
                                .horarioFim(LocalTime.of(11, 0))
                                .status(StatusReserva.PENDENTE)
                                .build();

                when(reservationRepository.findById(200L)).thenReturn(Optional.of(reserva));
                when(reservationRepository.save(any())).thenReturn(reserva);

                ReservationResponse response = reservationService.cancelarReserva(200L);

                assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADA);
                assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar 403 ao tentar cancelar reserva de outro usuário")
        void deveLancar403_quandoCancelarReservaDeOutroUsuario() {
                Reservation reserva = Reservation.builder()
                                .id(201L)
                                .user(outroUsuario)
                                .resource(resource)
                                .data(LocalDate.now().plusDays(1))
                                .horarioInicio(LocalTime.of(10, 0))
                                .horarioFim(LocalTime.of(11, 0))
                                .status(StatusReserva.PENDENTE)
                                .build();

                when(reservationRepository.findById(201L)).thenReturn(Optional.of(reserva));

                assertThatThrownBy(() -> reservationService.cancelarReserva(201L))
                                .isInstanceOf(ResponseStatusException.class)
                                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                                                .isEqualTo(HttpStatus.FORBIDDEN));
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar cancelar reserva já cancelada")
        void deveLancarErro_quandoReservaJaCancelada() {
                Reservation reserva = Reservation.builder()
                                .id(202L)
                                .user(usuarioLogado)
                                .resource(resource)
                                .data(LocalDate.now().plusDays(1))
                                .horarioInicio(LocalTime.of(10, 0))
                                .horarioFim(LocalTime.of(11, 0))
                                .status(StatusReserva.CANCELADA)
                                .build();

                when(reservationRepository.findById(202L)).thenReturn(Optional.of(reserva));

                assertThatThrownBy(() -> reservationService.cancelarReserva(202L))
                                .isInstanceOf(ResponseStatusException.class)
                                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                                                .isEqualTo(HttpStatus.CONFLICT));
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar cancelar reserva já iniciada ou encerrada")
        void deveLancarErro_quandoReservaJaIniciada() {
                Reservation reserva = Reservation.builder()
                                .id(203L)
                                .user(usuarioLogado)
                                .resource(resource)
                                .data(LocalDate.now().minusDays(1))
                                .horarioInicio(LocalTime.of(10, 0))
                                .horarioFim(LocalTime.of(11, 0))
                                .status(StatusReserva.PENDENTE)
                                .build();

                when(reservationRepository.findById(203L)).thenReturn(Optional.of(reserva));

                assertThatThrownBy(() -> reservationService.cancelarReserva(203L))
                                .isInstanceOf(ResponseStatusException.class)
                                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                                                .isEqualTo(HttpStatus.CONFLICT));
        }
}