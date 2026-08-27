package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationAdminResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceAdminTest {

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

    private Resource resource;
    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Maria Admin")
                .email("maria@ufape.br")
                .role(Role.USER)
                .build();

        resource = Resource.builder()
                .id(10L)
                .nome("Laboratório A")
                .tipo(TipoRecurso.LABORATORIO)
                .build();
    }

    @Test
    @DisplayName("Deve retornar lista paginada de reservas do recurso quando ele existe e tem reservas")
    void deveRetornarReservasPaginadasDoRecurso() {
        Reservation reserva = Reservation.builder()
                .id(100L)
                .user(usuario)
                .resource(resource)
                .data(LocalDate.of(2026, 9, 10))
                .horarioInicio(LocalTime.of(8, 0))
                .horarioFim(LocalTime.of(9, 0))
                .status(StatusReserva.CONFIRMADA)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> paginaMock = new PageImpl<>(List.of(reserva), pageable, 1);

        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(reservationRepository.findByResourceOrderByDataDescHorarioInicioDesc(resource, pageable))
                .thenReturn(paginaMock);

        Page<ReservationAdminResponse> resultado = reservationService.listarReservasPorRecurso(10L, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        ReservationAdminResponse item = resultado.getContent().get(0);
        assertThat(item.getId()).isEqualTo(100L);
        assertThat(item.getUserNome()).isEqualTo("Maria Admin");
        assertThat(item.getUserEmail()).isEqualTo("maria@ufape.br");
        assertThat(item.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);

        verify(reservationRepository).findByResourceOrderByDataDescHorarioInicioDesc(resource, pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando o recurso não tem nenhuma reserva")
    void deveRetornarPaginaVazia_quandoRecursoSemReservas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> paginaVazia = new PageImpl<>(List.of(), pageable, 0);

        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(reservationRepository.findByResourceOrderByDataDescHorarioInicioDesc(resource, pageable))
                .thenReturn(paginaVazia);

        Page<ReservationAdminResponse> resultado = reservationService.listarReservasPorRecurso(10L, pageable);

        assertThat(resultado.getTotalElements()).isZero();
        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar 404 quando o recurso não for encontrado")
    void deveLancar404_quandoRecursoNaoEncontrado() {
        Pageable pageable = PageRequest.of(0, 10);
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.listarReservasPorRecurso(99L, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
