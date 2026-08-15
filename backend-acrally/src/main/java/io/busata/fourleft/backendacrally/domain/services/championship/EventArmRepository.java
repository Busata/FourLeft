package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    /** Whether the user has a standing (non-reverted) arm with the given outcome for a stage — the
     *  one-shot DNF check (covers both an idle-EXPIRED arm and a CONSUMED one whose bound run was
     *  abandoned). A DNF a club owner reverted no longer spends the shot. */
    boolean existsByUserIdAndEventIdAndVariantIdAndOutcomeAndRevertedAtIsNull(
            UUID userId, UUID eventId, UUID variantId, EventArmOutcome outcome);

    /** The user's standing arms with a given outcome for an event — marks used-up stages in the
     *  races list; reverted DNFs are excluded so the stage opens back up in the agent. */
    List<EventArm> findAllByUserIdAndEventIdAndOutcomeAndRevertedAtIsNull(
            UUID userId, UUID eventId, EventArmOutcome outcome);

    /** Every arm with a given outcome across an event, freshest first — the owner's DNF panel.
     *  Reverted arms are included; the panel shows them as already handled. */
    List<EventArm> findAllByEventIdAndOutcomeOrderByUpdatedAtDesc(UUID eventId, EventArmOutcome outcome);

    /** ARMED arms with no activity (bind/unbind or the arming itself) since the cutoff. */
    @Query("""
            select a from EventArm a
            where a.status = io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus.ARMED
              and coalesce(a.updatedAt, a.armedAt) < :cutoff
            """)
    List<EventArm> findArmedAndIdleSince(@Param("cutoff") LocalDateTime cutoff);
}
