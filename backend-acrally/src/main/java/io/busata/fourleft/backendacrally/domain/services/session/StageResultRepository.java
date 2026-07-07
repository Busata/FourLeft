package io.busata.fourleft.backendacrally.domain.services.session;

import io.busata.fourleft.backendacrally.domain.models.session.StageResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StageResultRepository extends JpaRepository<StageResult, UUID> {

    /**
     * Idempotency lookup. The tick is stamped at *event entry* and shared by every run of that
     * event (the game overwrites the event's save slot per run), so the total time is part of
     * the key — ticks alone would swallow a second run of the same event as a "retry".
     */
    Optional<StageResult> findByUserIdAndTimestampTicksAndTotalMs(UUID userId, long timestampTicks, int totalMs);

    List<StageResult> findTop100ByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Every distinct, non-blank raw stage identifier seen across all recorded results. */
    @Query("SELECT DISTINCT s.stage FROM StageResult s WHERE s.stage IS NOT NULL AND s.stage <> ''")
    List<String> findDistinctStageNames();

    /** Every distinct, non-blank raw car string seen across all recorded results (for car aliases). */
    @Query("SELECT DISTINCT s.car FROM StageResult s WHERE s.car IS NOT NULL AND s.car <> ''")
    List<String> findDistinctCarNames();
}
