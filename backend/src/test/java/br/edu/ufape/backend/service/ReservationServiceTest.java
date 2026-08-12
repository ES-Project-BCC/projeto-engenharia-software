package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ReservationRequest;
import br.edu.ufape.backend.dto.ReservationResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.User;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import br.edu.ufape.backend.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createReservation throws NOT_FOUND when resource not exists")
    void createReservationResourceNotFound() {
        ReservationRequest req = new ReservationRequest(999L, LocalDate.now(), LocalTime.of(10,0), LocalTime.of(11,0));
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(req))
                .isInstanceOf(ResponseStatusException.class);

        verify(resourceRepository).findById(999L);
    }

    @Test
    @DisplayName("createReservation throws CONFLICT when reservation overlaps")
    void createReservationConflict() {
        Resource resource = Resource.builder().id(1L).nome("Sala").descricao("x").build();
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        ReservationRequest req = new ReservationRequest(1L, LocalDate.now(), LocalTime.of(10,0), LocalTime.of(11,0));

        when(reservationRepository.existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(req))
                .isInstanceOf(ResponseStatusException.class);

        verify(reservationRepository).existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                eq(resource), eq(req.getData()), eq(req.getHorarioInicio()), eq(req.getHorarioFim()));
    }

    @Test
    @DisplayName("createReservation saves and returns response when available")
    void createReservationSuccess() {
        Resource resource = Resource.builder().id(1L).nome("Sala").descricao("x").build();
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        ReservationRequest req = new ReservationRequest(1L, LocalDate.now(), LocalTime.of(12,0), LocalTime.of(13,0));

        when(reservationRepository.existsByResourceAndDataAndHorarioInicioLessThanAndHorarioFimGreaterThan(
                any(), any(), any(), any())).thenReturn(false);

        User user = User.builder().id(42L).nome("Test").email("t@test.com").password("p").role(br.edu.ufape.backend.model.Role.USER).build();
        Reservation saved = Reservation.builder().id(10L).resource(resource).user(user).data(req.getData()).horarioInicio(req.getHorarioInicio()).horarioFim(req.getHorarioFim()).build();
        when(reservationRepository.save(any())).thenReturn(saved);

        // mock authenticated principal
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null));

        ReservationResponse response = reservationService.createReservation(req);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getResourceId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(42L);

        verify(reservationRepository).save(any(Reservation.class));
    }
}
