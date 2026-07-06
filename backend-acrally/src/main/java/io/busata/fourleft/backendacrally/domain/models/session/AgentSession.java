package io.busata.fourleft.backendacrally.domain.models.session;

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

@Entity
@Table(name = "agent_session")
@Getter
@NoArgsConstructor
public class AgentSession {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "api_key_id", nullable = false)
    private UUID apiKeyId;

    private String driver;
    private String car;
    private String stage;
    private String track;

    @Column(name = "started_at_ms")
    private Long startedAtMs;

    @Column(name = "agent_version")
    private String agentVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(name = "abort_reason")
    private String abortReason;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "current_ms")
    private Integer currentMs;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Column(name = "distance_m")
    private Double distanceM;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AgentSession(UUID userId, UUID apiKeyId, String driver, String car, String stage, String track,
                        Long startedAtMs, String agentVersion) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        this.driver = driver;
        this.car = car;
        this.stage = stage;
        this.track = track;
        this.startedAtMs = startedAtMs;
        this.agentVersion = agentVersion;
        this.status = SessionStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public void heartbeat(Integer currentMs, Double speedKmh, Double distanceM) {
        this.lastHeartbeatAt = LocalDateTime.now();
        this.currentMs = currentMs;
        this.speedKmh = speedKmh;
        this.distanceM = distanceM;
    }

    public void complete() {
        this.status = SessionStatus.COMPLETED;
    }

    public void abort(String reason) {
        this.status = SessionStatus.ABORTED;
        this.abortReason = reason;
    }
}
