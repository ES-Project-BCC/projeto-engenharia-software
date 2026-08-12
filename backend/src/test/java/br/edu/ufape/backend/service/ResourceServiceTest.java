package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    @Test
    void deveCriarRecursoComSucessoQuandoStatusFuncionamentoForFornecido() {
        ResourceRequest request = new ResourceRequest("Sala 01", "Laboratório de Informática", 30, "LABORATORIO", false);
        
        Resource resourceSalvo = Resource.builder()
                .id(1L)
                .nome("Sala 01")
                .descricao("Laboratório de Informática")
                .capacidade(30)
                .tipo("LABORATORIO")
                .statusFuncionamento(false)
                .build();

        when(resourceRepository.save(any(Resource.class))).thenReturn(resourceSalvo);

        ResourceResponse response = resourceService.criarRecurso(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sala 01", response.getNome());
        assertEquals("Laboratório de Informática", response.getDescricao());
        assertEquals(30, response.getCapacidade());
        assertEquals("LABORATORIO", response.getTipo());
        assertFalse(response.getStatusFuncionamento());

        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    void deveCriarRecursoComStatusTrueQuandoStatusFuncionamentoForNulo() {
        ResourceRequest request = new ResourceRequest("Auditório", "Auditório Principal", 100, "AUDITORIO", null);
        
        Resource resourceSalvo = Resource.builder()
                .id(2L)
                .nome("Auditório")
                .descricao("Auditório Principal")
                .capacidade(100)
                .tipo("AUDITORIO")
                .statusFuncionamento(true) // O Service deve transformar o null em true
                .build();

        when(resourceRepository.save(any(Resource.class))).thenReturn(resourceSalvo);

        ResourceResponse response = resourceService.criarRecurso(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertTrue(response.getStatusFuncionamento()); // Valida a regra de fallback para true

        verify(resourceRepository, times(1)).save(any(Resource.class));
    }
}
