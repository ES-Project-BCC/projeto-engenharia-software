package br.edu.ufape.backend.repository;

import br.edu.ufape.backend.model.Resource;
import br.edu.ufape.backend.model.ResourceBlock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceBlockRepository extends JpaRepository<ResourceBlock, Long> {

    @Query("SELECT b.resource.id FROM ResourceBlock b " +
            "WHERE b.inicio < :fim AND b.fim > :inicio")
    List<Long> findBlockedResourceIds(
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<ResourceBlock> findByResourceOrderByInicioDesc(Resource resource);
}