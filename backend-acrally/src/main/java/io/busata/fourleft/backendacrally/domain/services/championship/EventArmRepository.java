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

    /**
     * Live arms with no activity (bind/unbind or the arming itself) since the cutoff: ARMED ones
     * whose run never came, plus BOUND ones whose session has since ended without ever producing a
     * record. The latter would otherwise sit forever — the stale-session sweep only looks at OPEN
     * sessions, and a BOUND arm can be neither disarmed nor superseded.
     */
    @Query("""
            select a from EventArm a
            where coalesce(a.updatedAt, a.armedAt) < :cutoff
              and (a.status = io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus.ARMED
                   or (a.status = io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus.BOUND
                       and exists (select 1 from AgentSession s
                                   where s.id = a.sessionId
                                     and s.status <> io.busata.fourleft.backendacrally.domain.models.session.SessionStatus.OPEN)))
            """)
    List<EventArm> findIdleSince(@Param("cutoff") LocalDateTime cutoff);
}
