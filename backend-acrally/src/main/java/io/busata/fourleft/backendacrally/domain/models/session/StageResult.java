package io.busata.fourleft.backendacrally.domain.models.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_result")
@Getter
@NoArgsConstructor
public class StageResult {

    @Id
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String stage;
    private String car;
    private String driver;

    @Column(name = "raw_ms", nullable = false)
    private int rawMs;

    @Column(name = "penalty_ms", nullable = false)
    private int penaltyMs;

    @Column(name = "total_ms", nullable = false)
    private int totalMs;

    @Column(name = "timestamp_ticks", nullable = false)
    private long timestampTicks;

    @Column(name = "agent_version")
    private String agentVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public StageResult(UUID sessionId, UUID userId, String stage, String car, String driver,
                       int rawMs, int penaltyMs, int totalMs, long timestampTicks, String agentVersion) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.userId = userId;
        this.stage = stage;
        this.car = car;
        this.driver = driver;
        this.rawMs = rawMs;
        this.penaltyMs = penaltyMs;
        this.totalMs = totalMs;
        this.timestampTicks = timestampTicks;
        this.agentVersion = agentVersion;
        this.createdAt = LocalDateTime.now();
    }
}
