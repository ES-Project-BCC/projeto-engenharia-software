package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.AvailabilityResponse;
import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.ResourceBlockRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceBlockRepository resourceBlockRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource lab1;
    private Resource equip2;

    private final LocalDate data = LocalDate.of(2026, 9, 1);
    private final LocalTime inicio = LocalTime.of(10, 0);
    private final LocalTime fim = LocalTime.of(12, 0);

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

    // teste 1: sem nenhuma reserva conflitante, todos devem aparecer como
    // disponivel
    @Test
    @DisplayName("Deve retornar todos os recursos como disponíveis quando não há conflitos")
    void deveRetornarTodosDisponiveis_quandoSemConflito() {
        when(resourceRepository.findAll()).thenReturn(List.of(lab1, equip2));
        when(reservationRepository.findConflictingResourceIds(
                eq(data), eq(inicio), eq(fim), anyList()))
                .thenReturn(List.of());
        // sem bloqueios administrativos no periodo
        when(resourceBlockRepository.findBlockedResourceIds(any(), any()))
                .thenReturn(List.of());

        AvailabilityRequest request = new AvailabilityRequest(data, inicio, fim);
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
        when(reservationRepository.findConflictingResourceIds(eq(data), eq(inicio), eq(fim), anyList()))
                .thenReturn(List.of(1L));
        // sem bloqueios administrativos
        when(resourceBlockRepository.findBlockedResourceIds(any(), any()))
                .thenReturn(List.of());

        AvailabilityRequest request = new AvailabilityRequest(data, inicio, fim);
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
                data, inicio, fim,
                List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA)))
                .thenReturn(List.of());
        // sem bloqueios administrativos
        when(resourceBlockRepository.findBlockedResourceIds(any(), any()))
                .thenReturn(List.of());

        AvailabilityRequest request = new AvailabilityRequest(data, inicio, fim);
        List<AvailabilityResponse> result = resourceService.consultarDisponibilidade(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDisponivel()).isTrue();
    }

    // teste 3b (US12): recurso com bloqueio administrativo ativo no período consultado
    // deve aparecer como indisponível, mesmo sem nenhuma reserva conflitante
    @Test
    @DisplayName("Deve marcar recurso como indisponível quando há bloqueio administrativo no período (US12)")
    void deveMarcarIndisponivel_quandoHaBloqueioAdministrativo() {
        when(resourceRepository.findAll()).thenReturn(List.of(lab1, equip2));
        // nenhuma reserva conflitante
        when(reservationRepository.findConflictingResourceIds(eq(data), eq(inicio), eq(fim), anyList()))
                .thenReturn(List.of());
        // lab1 (id=1) está bloqueado administrativamente no período consultado
        when(resourceBlockRepository.findBlockedResourceIds(any(), any()))
                .thenReturn(List.of(1L));

        AvailabilityRequest request = new AvailabilityRequest(data, inicio, fim);
        List<AvailabilityResponse> result = resourceService.consultarDisponibilidade(request);

        assertThat(result).hasSize(2);
        AvailabilityResponse lab = result.stream().filter(r -> r.getId().equals(1L)).findFirst().orElseThrow();
        AvailabilityResponse equip = result.stream().filter(r -> r.getId().equals(2L)).findFirst().orElseThrow();

        assertThat(lab.isDisponivel()).isFalse();
        assertThat(equip.isDisponivel()).isTrue();
    }

    // teste 4: horario invalido, fim antes do inicio deve dar erro 400
    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando horarioFim <= horarioInicio")
    void deveLancarBadRequest_quandoHorarioInvalido() {
        LocalTime inicioTarde = LocalTime.of(14, 0);
        LocalTime fimCedo = LocalTime.of(13, 0); // fim antes do inicio

        AvailabilityRequest request = new AvailabilityRequest(data, inicioTarde, fimCedo);

        assertThatThrownBy(() -> resourceService.consultarDisponibilidade(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Horário de fim deve ser posterior ao horário de início");
    }

    // teste 5: inicio igual ao fim tambem e invalido
    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando horarioFim igual ao horarioInicio")
    void deveLancarBadRequest_quandoHorarioInicioIgualFim() {
        LocalTime mesmoHorario = LocalTime.of(10, 0);

        AvailabilityRequest request = new AvailabilityRequest(data, mesmoHorario, mesmoHorario);

        assertThatThrownBy(() -> resourceService.consultarDisponibilidade(request))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ---- testes de editarRecurso (task #146, #147) ----

    // teste 6: edicao com sucesso - deve retornar o resource atualizado
    @Test
    @DisplayName("Deve editar e retornar o recurso atualizado quando o id existe")
    void deveEditarRecurso_quandoIdExiste() {
        ResourceRequest request = new ResourceRequest("Lab B", "Lab atualizado", 40, TipoRecurso.LABORATORIO, false);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(lab1));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceResponse response = resourceService.editarRecurso(1L, request);

        assertThat(response.getNome()).isEqualTo("Lab B");
        assertThat(response.getDescricao()).isEqualTo("Lab atualizado");
        assertThat(response.getCapacidade()).isEqualTo(40);
        assertThat(response.getTipo()).isEqualTo(TipoRecurso.LABORATORIO);
        assertThat(response.getStatusFuncionamento()).isFalse();
    }

    // teste 7: edicao com id inexistente deve lancar 404 NOT_FOUND
    @Test
    @DisplayName("Deve lançar 404 NOT_FOUND quando o id do recurso não existe")
    void deveLancar404_quandoIdNaoExiste() {
        ResourceRequest request = new ResourceRequest("Qualquer", "Desc", 10, TipoRecurso.EQUIPAMENTO, true);

        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.editarRecurso(999L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // teste 8: statusFuncionamento null no request nao deve sobrescrever o valor atual
    @Test
    @DisplayName("Deve manter statusFuncionamento atual quando request envia null")
    void deveManter_statusFuncionamento_quandoRequestEnviaNull() {
        ResourceRequest request = new ResourceRequest("Lab A", "Lab de redes", 30, TipoRecurso.LABORATORIO, null);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(lab1));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceResponse response = resourceService.editarRecurso(1L, request);

        // lab1 foi criado com statusFuncionamento=true, e o request mandou null
        assertThat(response.getStatusFuncionamento()).isTrue();
    }
}