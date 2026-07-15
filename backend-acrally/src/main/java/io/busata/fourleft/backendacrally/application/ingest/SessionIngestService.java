package io.busata.fourleft.backendacrally.application.ingest;

import io.busata.fourleft.backendacrally.domain.models.session.AgentSession;
import io.busata.fourleft.backendacrally.domain.models.session.StageResult;
import io.busata.fourleft.backendacrally.domain.services.championship.EventRecordingService;
import io.busata.fourleft.backendacrally.domain.services.session.AgentSessionRepository;
import io.busata.fourleft.backendacrally.domain.services.session.StageResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionIngestService {

    private final AgentSessionRepository sessions;
    private final StageResultRepository results;
    private final EventRecordingService recordingService;

    /** Seconds between 0001-01-01 (the .NET-ticks epoch) and 1970-01-01. */
    private static final long UNIX_EPOCH_TICKS_SECONDS = 62_135_596_800L;

    /** Clock-skew allowance on the future bound: two days, in ticks. */
    private static final long FUTURE_MARGIN_TICKS = 2L * 24 * 3600 * 10_000_000;

    @Transactional
    public UUID open(UUID userId, UUID apiKeyId, IngestPayloads.SessionStart p) {
        AgentSession session = new AgentSession(
                userId, apiKeyId, p.driver(), p.car(), p.stage(), p.track(), p.startedAtMs(), p.agentVersion());
        UUID sessionId = sessions.save(session).getId();
        // Bind a waiting arm to this fresh run: a session that opened before the arm existed can't
        // bind here, so pressing Start mid-run never captures the run in progress.
        recordingService.bindToSession(userId, sessionId);
        return sessionId;
    }

    @Transactional
    public void heartbeat(UUID userId, String rawSessionId, IngestPayloads.Heartbeat p) {
        AgentSession session = ownedSession(userId, rawSessionId);
        session.heartbeat(p.currentMs(), p.speedKmh(), p.distanceM());
    }

    @Transactional
    public void recordResult(UUID userId, String rawSessionId, IngestPayloads.Result p) {
        AgentSession session = ownedSession(userId, rawSessionId);
        // A record's tick is stamped at event entry from the player's (UTC) clock, so a tick
        // meaningfully in the future is a false save anchor, not a run (2026-07-09: a
        // ServiceParkDefault block decoding to year 2057 was submitted with a stage name in the
        // car field and poisoned that agent's result floor). Reject it so garbage can't enter
        // the results or the car-alias catalogue; fixed agents never send one.
        long maxPlausibleTicks =
                (Instant.now().getEpochSecond() + UNIX_EPOCH_TICKS_SECONDS) * 10_000_000L + FUTURE_MARGIN_TICKS;
        if (p.timestampTicks() > maxPlausibleTicks) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Result timestamp is in the future — rejected as a false save anchor.");
        }
        // De-dupe by save-file tick + total: retries re-deliver the same result (contract
        // §Delivery), but the tick alone is shared by every run of one event entry — a second
        // run of the same event arrives with the same tick and different times, and is NOT a dupe.
        if (results.findByUserIdAndTimestampTicksAndTotalMs(userId, p.timestampTicks(), p.totalMs()).isEmpty()) {
            try {
                StageResult saved = results.save(new StageResult(
                        session.getId(), userId, p.stage(), p.car(), p.driver(),
                        p.rawMs(), p.penaltyMs(), p.totalMs(), p.timestampTicks(), p.agentVersion()));
                // Flush so the row exists before an event_entry references it (FK ordering).
                results.flush();
                // First delivery only: score it against any arm bound to this session.
                recordingService.recordIfArmed(session.getId(), saved);
            } catch (DataIntegrityViolationException concurrentDuplicate) {
                // Lost a race with a concurrent delivery of the same tick — already stored and scored.
            }
        }
        session.complete();
    }

    @Transactional
    public void abort(UUID userId, String rawSessionId, String reason) {
        AgentSession session = ownedSession(userId, rawSessionId);
        session.abort(reason);
        // A restart/quit shouldn't burn the arm — release it so the driver's next run re-binds.
        recordingService.unbindSession(session.getId());
    }

    /**
     * Janitor half of the contract: heartbeats/aborts are at-most-once, so a session with no
     * result and no abort must be timed out rather than waited on forever. Marks long-silent
     * OPEN sessions STALE and releases any arm still bound to them, so a driver whose agent
     * died mid-run gets their arm back. Returns how many were swept (for the schedule's log).
     */
    @Transactional
    public int sweepStaleSessions(LocalDateTime cutoff) {
        List<AgentSession> silent = sessions.findOpenAndSilentSince(cutoff);
        for (AgentSession session : silent) {
            session.markStale();
            recordingService.unbindSession(session.getId());
        }
        return silent.size();
    }

    /**
     * Resolves a server-issued session owned by this user. Non-UUID ids (the agent's
     * {@code local-<ns>} fallback when POST /sessions failed) and unknown/other-user ids are
     * rejected — the contract calls this a valid server policy that closes a forgery avenue.
     */
    private AgentSession ownedSession(UUID userId, String rawSessionId) {
        UUID id;
        try {
            id = UUID.fromString(rawSessionId);
        } catch (IllegalArgumentException notServerIssued) {
            throw notFound();
        }
        return sessions.findById(id)
                .filter(session -> session.getUserId().equals(userId))
                .orElseThrow(this::notFound);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown session.");
    }
}
