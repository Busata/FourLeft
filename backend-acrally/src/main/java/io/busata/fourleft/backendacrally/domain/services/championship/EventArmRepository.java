package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventArmRepository extends JpaRepository<EventArm, UUID> {

    /** The user's live arm (ARMED or BOUND) — the unique index guarantees at most one. */
    Optional<EventArm> findFirstByUserIdAndStatusIn(UUID userId, List<EventArmStatus> statuses);

    /** The arm bound to a given session, if any (used by the ingestion pipeline). */
    Optional<EventArm> findFirstBySessionIdAndStatus(UUID sessionId, EventArmStatus status);

    /** The user's most recent arm regardless of status — for showing the last outcome. */
    Optional<EventArm> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
