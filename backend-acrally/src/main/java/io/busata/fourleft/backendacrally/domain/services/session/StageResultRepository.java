package io.busata.fourleft.backendacrally.domain.services.session;

import io.busata.fourleft.backendacrally.domain.models.session.StageResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StageResultRepository extends JpaRepository<StageResult, UUID> {

    Optional<StageResult> findByUserIdAndTimestampTicks(UUID userId, long timestampTicks);

    List<StageResult> findTop100ByUserIdOrderByCreatedAtDesc(UUID userId);
}
