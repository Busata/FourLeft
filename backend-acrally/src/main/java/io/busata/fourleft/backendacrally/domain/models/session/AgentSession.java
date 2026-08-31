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

    /** The agent restarted the run mid-stage — the run was thrown away. */
    public static final String ABORT_RESTART = "restart";
    /** A new run began while this session was still live — the old run was thrown away. */
    public static final String ABORT_SUPERSEDED = "superseded";
    /** No save record turned up inside the agent's wait window. Says nothing about the run. */
    public static final String ABORT_NO_RESULT = "no-result";

    /**
     * Whether this session's end proves the driver threw the run away, which is what spends an
     * armed stage's one shot. Only a restart or a superseding run prove it.
     *
     * <p>{@link #ABORT_NO_RESULT} does not: it is the agent reporting that it never saw a save
     * record, and its watcher keeps looking afterwards. Treating it as an abandoned run cost real
     * drivers completed stages — 2026-08-16, a finish misdetected 34s into a 4:18 run started the
     * agent's 3-minute save-wait early, the wait lapsed, the arm was DNF'd, and the genuine record
     * arrived 86 seconds later on this very session with nothing left to score.
     */
    public boolean provesAbandonedRun() {
        return status == SessionStatus.ABORTED
                && (ABORT_RESTART.equals(abortReason) || ABORT_SUPERSEDED.equals(abortReason));
    }

    /** Swept by the janitor: still OPEN long after the agent stopped reporting. */
    public void markStale() {
        this.status = SessionStatus.STALE;
    }
}
