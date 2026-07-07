package io.busata.fourleft.backendacrally.domain.models.championship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An ordered round within a {@link Championship}. It carries no stored name — it's labelled on read
 * from the distinct locations of the stages it runs. Its calendar window is derived, not stored:
 * {@code gapDays} is the pause before it opens (from the championship start for the first event,
 * otherwise from the prior event's close) and {@code durationDays} is how long it stays open.
 */
@Entity
@Table(name = "championship_event")
@Getter
@NoArgsConstructor
public class ChampionshipEvent {

    @Id
    private UUID id;

    @Column(name = "championship_id", nullable = false, updatable = false)
    private UUID championshipId;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "gap_days", nullable = false)
    private int gapDays;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ChampionshipEvent(UUID championshipId, int position, int gapDays, int durationDays) {
        this.id = UUID.randomUUID();
        this.championshipId = championshipId;
        this.position = position;
        this.gapDays = gapDays;
        this.durationDays = durationDays;
        this.createdAt = LocalDateTime.now();
    }

    public void update(int gapDays, int durationDays) {
        this.gapDays = gapDays;
        this.durationDays = durationDays;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPosition(int position) {
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }
}
