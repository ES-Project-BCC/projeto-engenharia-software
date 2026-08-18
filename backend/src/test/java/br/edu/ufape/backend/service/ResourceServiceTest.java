package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.AvailabilityResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource lab1;
    private Resource equip2;

    private final LocalDate DATA = LocalDate.of(2026, 9, 1);
    private final LocalTime INICIO = LocalTime.of(10, 0);
    private final LocalTime FIM = LocalTime.of(12, 0);

    @BeforeEach
    void setUp() {
        lab1 = Resource.builder()
                .id(1L)
                .nome("Laboratório A")
                .descricao("Lab de redes")
                .capacidade(30)
                .tipo(TipoRecurso.LABORATORIO)
                .statusFuncionamento(true)
                .build();

        equip2 = Resource.builder()
                .id(2L)
                .nome("Projetor X")
                .descricao("Projetor Full HD")
                .capacidade(1)
                .tipo(TipoRecurso.EQUIPAMENTO)
                .statusFuncionamento(true)
                .build();
    }

    // teste 1: sem nenhuma reserva conflitante, todos devem aparecer como disponivel
    @Test
    @DisplayName("Deve retornar todos os recursos como disponíveis quando não há conflitos")
    void deveRetornarTodosDisponiveis_quandoSemConflito() {
        when(resourceRepository.findAll()).thenReturn(List.of(lab1, equip2));
        when(reservationRepository.findConflictingResourceIds(eq(DATA), eq(INICIO), eq(FIM), anyList()))
                .thenReturn(List.of());

        AvailabilityRequest request = new AvailabilityRequest(DATA, INICIO, FIM);
        List<AvailabilityResponse> result = resourceService.consultarDisponibilidade(request);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(AvailabilityResponse::isDisponivel);
    }

    // teste 2: o lab ta ocupado, o equipamento deve continuar disponivel
    @Test
    @DisplayName("Deve marcar recurso como indisponível quando há reserva ativa conflitante")
    void deveMarcarIndisponivel_quandoHaConflito() {
        when(resourceRepository.findAll()).thenReturn(List.of(lab1, equip2));
        // simula que o lab1 (id=1) tem conflito de horario
        when(reservationRepository.findConflictingResourceIds(eq(DATA), eq(INICIO), eq(FIM), anyList()))
                .thenReturn(List.of(1L));

        AvailabilityRequest request = new AvailabilityRequest(DATA, INICIO, FIM);
        List<AvailabilityResponse> result = resourceService.consultarDisponibilidade(request);

        assertThat(result).hasSize(2);
        AvailabilityResponse lab = result.stream().filter(r -> r.getId().equals(1L)).findFirst().orElseThrow();
        AvailabilityResponse equip = result.stream().filter(r -> r.getId().equals(2L)).findFirst().orElseThrow();

        assertThat(lab.isDisponivel()).isFalse();
        assertThat(equip.isDisponivel()).isTrue();
    }

    // teste 3: reserva cancelada nao pode bloquear o horario (task #84)
    // a gente so passa PENDENTE e CONFIRMADA pro repositorio
    @Test
    @DisplayName("Reserva CANCELADA não deve bloquear disponibilidade (task #84)")
    void reservaCancelada_naoDeveBloquearDisponibilidade() {
        when(resourceRepository.findAll()).thenReturn(List.of(lab1));
        // repositorio retorna lista vazia porque a reserva cancelada foi filtrada
        when(reservationRepository.findConflictingResourceIds(
                eq(DATA), eq(INICIO), eq(FIM),
                eq(List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA))))
                .thenReturn(List.of());

        AvailabilityRequest request = new AvailabilityRequest(DATA, INICIO, FIM);
        List<AvailabilityResponse> result = resourceService.consultarDisponibilidade(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDisponivel()).isTrue();
    }

    // teste 4: horario invalido, fim antes do inicio deve dar erro 400
    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando horarioFim <= horarioInicio")
    void deveLancarBadRequest_quandoHorarioInvalido() {
        LocalTime inicioTarde = LocalTime.of(14, 0);
        LocalTime fimCedo = LocalTime.of(13, 0); // fim antes do inicio

        AvailabilityRequest request = new AvailabilityRequest(DATA, inicioTarde, fimCedo);

        assertThatThrownBy(() -> resourceService.consultarDisponibilidade(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Horário de fim deve ser posterior ao horário de início");
    }

    // teste 5: inicio igual ao fim tambem e invalido
    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando horarioFim igual ao horarioInicio")
    void deveLancarBadRequest_quandoHorarioInicioIgualFim() {
        LocalTime mesmoHorario = LocalTime.of(10, 0);

        AvailabilityRequest request = new AvailabilityRequest(DATA, mesmoHorario, mesmoHorario);

        assertThatThrownBy(() -> resourceService.consultarDisponibilidade(request))
                .isInstanceOf(ResponseStatusException.class);
    }
}
