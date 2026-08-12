package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.enums.TipoRecurso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ResourceRepositoryTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("save persists and recovers resource correctly when all fields are valid")
    void shouldSaveAndFindResourceWhenDataIsValid() {
        Resource resource = Resource.builder()
                .nome("Sala de Reuniões")
                .descricao("Sala equipada com projetor e mesa de videoconferência")
                .capacidade(12)
                .tipo(TipoRecurso.SALA)
                .statusFuncionamento(true)
                .build();

        Resource savedResource = resourceRepository.save(resource);
        Optional<Resource> foundResource = resourceRepository.findById(savedResource.getId());

        assertThat(foundResource).isPresent();
        assertThat(foundResource.get().getNome()).isEqualTo("Sala de Reuniões");
        assertThat(foundResource.get().getDescricao()).isEqualTo("Sala equipada com projetor e mesa de videoconferência");
        assertThat(foundResource.get().getCapacidade()).isEqualTo(12);
        assertThat(foundResource.get().getTipo()).isEqualTo(TipoRecurso.SALA);
        assertThat(foundResource.get().getStatusFuncionamento()).isTrue();
    }

    @Test
    @DisplayName("save throws DataIntegrityViolationException when nome is null")
    void shouldThrowExceptionWhenNomeIsNull() {
        Resource resource = Resource.builder()
                .nome(null)
                .descricao("Laboratório de Redes")
                .tipo(TipoRecurso.LABORATORIO)
                .statusFuncionamento(true)
                .build();

        assertThatThrownBy(() -> {
            resourceRepository.save(resource);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("save throws DataIntegrityViolationException when descricao is null")
    void shouldThrowExceptionWhenDescricaoIsNull() {
        Resource resource = Resource.builder()
                .nome("Auditório Bloco A")
                .descricao(null)
                .tipo(TipoRecurso.AUDITORIO)
                .statusFuncionamento(true)
                .build();

        assertThatThrownBy(() -> {
            resourceRepository.save(resource);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("save throws DataIntegrityViolationException when tipo is null")
    void shouldThrowExceptionWhenTipoIsNull() {
        Resource resource = Resource.builder()
                .nome("Mini Auditório")
                .descricao("Mini auditório para defesas de TCC")
                .tipo(null)
                .statusFuncionamento(true)
                .build();

        assertThatThrownBy(() -> {
            resourceRepository.save(resource);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("save throws DataIntegrityViolationException when descricao length exceeds 500 characters")
    void shouldThrowExceptionWhenDescricaoExceedsMaxLength() {
        String longDescricao = "A".repeat(501);

        Resource resource = Resource.builder()
                .nome("Laboratório de Física")
                .descricao(longDescricao)
                .tipo(TipoRecurso.LABORATORIO)
                .statusFuncionamento(true)
                .build();

        assertThatThrownBy(() -> {
            resourceRepository.save(resource);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
