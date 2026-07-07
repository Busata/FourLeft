package io.busata.fourleft.backendacrally.domain.models.championship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A driver's "I'm about to drive this stage" record — the server side of the agent's Start button.
 * It binds to the next {@link io.busata.fourleft.backendacrally.domain.models.session.AgentSession}
 * the driver opens, so a run that started before the arm existed can never be captured. See
 * {@link EventArmStatus} for the lifecycle.
 */
@Entity
@Table(name = "event_arm")
@Getter
@NoArgsConstructor
public class EventArm {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventArmStatus status;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "result_id")
    private UUID resultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private EventArmOutcome outcome;

    @Column(name = "armed_at", nullable = false, updatable = false)
    private LocalDateTime armedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public EventArm(UUID userId, UUID eventId, UUID variantId) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.eventId = eventId;
        this.variantId = variantId;
        this.status = EventArmStatus.ARMED;
        this.armedAt = now;
        this.createdAt = now;
    }

    /** Attach this arm to a freshly opened session (ARMED → BOUND). */
    public void bind(UUID sessionId) {
        this.sessionId = sessionId;
        this.status = EventArmStatus.BOUND;
        this.updatedAt = LocalDateTime.now();
    }

    /** The bound run was abandoned (abort/restart) — release so the next run can re-bind. */
    public void unbind() {
        this.sessionId = null;
        this.status = EventArmStatus.ARMED;
        this.updatedAt = LocalDateTime.now();
    }

    /** The bound run finished — record how it scored. */
    public void consume(EventArmOutcome outcome, UUID resultId) {
        this.status = EventArmStatus.CONSUMED;
        this.outcome = outcome;
        this.resultId = resultId;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = EventArmStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Armed but no run ever completed — timed out by the janitor as a DNF. */
    public void expire() {
        this.status = EventArmStatus.EXPIRED;
        this.outcome = EventArmOutcome.DNF;
        this.updatedAt = LocalDateTime.now();
    }
}
