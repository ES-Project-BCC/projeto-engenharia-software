package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ResourceBlockRequest;
import br.edu.ufape.backend.dto.ResourceBlockResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.ResourceBlock;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import br.edu.ufape.backend.repository.ResourceBlockRepository;
import br.edu.ufape.backend.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes da US12 - Bloqueio administrativo de recursos.
 * Cobre a criação de bloqueios (sucesso e validação de horário).
 */
@ExtendWith(MockitoExtension.class)
class ResourceBlockServiceTest {

    @Mock
    private ResourceBlockRepository resourceBlockRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceBlockService resourceBlockService;

    private Resource lab1;

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
    }

    // teste 1: bloqueio valido deve ser criado com sucesso (equivalente ao 201 retornado pelo controller)
    @Test
    @DisplayName("Deve criar bloqueio com sucesso quando os dados são válidos (US12)")
    void deveCriarBloqueio_quandoDadosValidos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 1, 12, 0);

        ResourceBlockRequest request = new ResourceBlockRequest(
                lab1.getId(), inicio, fim, "Manutenção preventiva");

        when(resourceRepository.findById(lab1.getId())).thenReturn(Optional.of(lab1));
        when(resourceBlockRepository.save(any(ResourceBlock.class))).thenAnswer(inv -> {
            ResourceBlock block = inv.getArgument(0);
            block.setId(10L);
            return block;
        });

        ResourceBlockResponse response = resourceBlockService.criarBloqueio(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getResourceId()).isEqualTo(lab1.getId());
        assertThat(response.getResourceNome()).isEqualTo("Laboratório A");
        assertThat(response.getInicio()).isEqualTo(inicio);
        assertThat(response.getFim()).isEqualTo(fim);
        assertThat(response.getMotivo()).isEqualTo("Manutenção preventiva");

        verify(resourceBlockRepository).save(any(ResourceBlock.class));
    }

    // teste 2: fim <= inicio deve lançar 400 BAD_REQUEST (fim antes do inicio)
    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando o fim do bloqueio é anterior ao início (US12)")
    void deveLancarBadRequest_quandoFimAnteriorAoInicio() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 1, 10, 0); // fim antes do inicio

        ResourceBlockRequest request = new ResourceBlockRequest(
                lab1.getId(), inicio, fim, "Manutenção");

        assertThatThrownBy(() -> resourceBlockService.criarBloqueio(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("A data/hora de fim deve ser maior que a de início");

        // nao deve nem consultar o recurso, ja falha na validação do horário
        verify(resourceRepository, never()).findById(any());
        verify(resourceBlockRepository, never()).save(any());
    }

    // teste 3: fim igual ao inicio tambem é invalido (fim <= inicio)
    @Test
    @DisplayName("Deve lançar 400 BAD_REQUEST quando o fim do bloqueio é igual ao início (US12)")
    void deveLancarBadRequest_quandoFimIgualAoInicio() {
        LocalDateTime mesmoInstante = LocalDateTime.of(2026, 9, 1, 10, 0);

        ResourceBlockRequest request = new ResourceBlockRequest(
                lab1.getId(), mesmoInstante, mesmoInstante, "Manutenção");

        assertThatThrownBy(() -> resourceBlockService.criarBloqueio(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(resourceBlockRepository, never()).save(any());
    }

    // teste 4: recurso inexistente deve lançar 404 (garante que a validação de horário
    // acontece antes, e a busca do recurso só ocorre quando o horário é válido)
    @Test
    @DisplayName("Deve lançar 404 NOT_FOUND quando o recurso do bloqueio não existe")
    void deveLancarNotFound_quandoRecursoNaoExiste() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 1, 12, 0);

        ResourceBlockRequest request = new ResourceBlockRequest(999L, inicio, fim, "Manutenção");

        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceBlockService.criarBloqueio(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(resourceBlockRepository, never()).save(any());
    }

    // teste 5: listarBloqueios deve retornar os bloqueios do recurso em ordem
    @Test
    @DisplayName("Deve listar os bloqueios de um recurso existente")
    void deveListarBloqueios_quandoRecursoExiste() {
        ResourceBlock block = ResourceBlock.builder()
                .id(5L)
                .resource(lab1)
                .inicio(LocalDateTime.of(2026, 9, 1, 8, 0))
                .fim(LocalDateTime.of(2026, 9, 1, 12, 0))
                .motivo("Manutenção")
                .build();

        when(resourceRepository.findById(lab1.getId())).thenReturn(Optional.of(lab1));
        when(resourceBlockRepository.findByResourceOrderByInicioDesc(lab1)).thenReturn(List.of(block));

        List<ResourceBlockResponse> resultado = resourceBlockService.listarBloqueios(lab1.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(5L);
    }

    // teste 6: remover bloqueio inexistente deve lançar 404
    @Test
    @DisplayName("Deve lançar 404 NOT_FOUND ao remover um bloqueio inexistente")
    void deveLancarNotFound_aoRemoverBloqueioInexistente() {
        when(resourceBlockRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceBlockService.removerBloqueio(123L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(resourceBlockRepository, never()).delete(any());
    }
}