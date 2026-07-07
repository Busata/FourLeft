package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StageRepository extends JpaRepository<Stage, UUID> {

    List<Stage> findAllByOrderByNameAsc();

    boolean existsByLocationId(UUID locationId);

    long countByLocationId(UUID locationId);
}
