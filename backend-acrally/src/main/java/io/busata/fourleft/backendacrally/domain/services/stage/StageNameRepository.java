package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.StageName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StageNameRepository extends JpaRepository<StageName, UUID> {

    List<StageName> findAllByOrderByRawNameAsc();
}
