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
 * A driver's best penalised time on one stage (variant) of an event — a leaderboard row. One row per
 * (event, variant, driver); a faster later run overwrites it in place. The event's overall standings
 * (sum of best per stage) are derived on read, not stored.
 */
@Entity
@Table(name = "event_entry")
@Getter
@NoArgsConstructor
public class EventEntry {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "car_name")
    private String carName;

    @Column(name = "result_id", nullable = false)
    private UUID resultId;

    @Column(name = "raw_ms", nullable = false)
    private int rawMs;

    @Column(name = "penalty_ms", nullable = false)
    private int penaltyMs;

    @Column(name = "total_ms", nullable = false)
    private int totalMs;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public EventEntry(UUID eventId, UUID variantId, UUID userId, UUID carId, String carName,
                      UUID resultId, int rawMs, int penaltyMs, int totalMs, LocalDateTime recordedAt) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.variantId = variantId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        applyTime(carId, carName, resultId, rawMs, penaltyMs, totalMs, recordedAt);
    }

    /** Overwrite this entry with a faster run's time. */
    public void replaceWith(UUID carId, String carName, UUID resultId,
                            int rawMs, int penaltyMs, int totalMs, LocalDateTime recordedAt) {
        applyTime(carId, carName, resultId, rawMs, penaltyMs, totalMs, recordedAt);
        this.updatedAt = LocalDateTime.now();
    }

    private void applyTime(UUID carId, String carName, UUID resultId,
                           int rawMs, int penaltyMs, int totalMs, LocalDateTime recordedAt) {
        this.carId = carId;
        this.carName = carName;
        this.resultId = resultId;
        this.rawMs = rawMs;
        this.penaltyMs = penaltyMs;
        this.totalMs = totalMs;
        this.recordedAt = recordedAt;
    }
}
