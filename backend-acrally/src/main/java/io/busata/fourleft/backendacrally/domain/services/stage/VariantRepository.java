package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VariantRepository extends JpaRepository<Variant, UUID> {

    List<Variant> findAllByOrderByRawNameAsc();

    /** Match an incoming result's raw stage key back to its collected variant (exact). */
    Optional<Variant> findByRawName(String rawName);

    boolean existsByStageId(UUID stageId);

    long countByStageId(UUID stageId);
}
