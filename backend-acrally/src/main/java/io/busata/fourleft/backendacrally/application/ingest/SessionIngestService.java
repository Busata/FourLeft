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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionIngestService {

    private final AgentSessionRepository sessions;
    private final StageResultRepository results;
    private final EventRecordingService recordingService;

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
        // De-dupe by the save-file tick: retries re-deliver the same result (contract §Delivery).
        if (results.findByUserIdAndTimestampTicks(userId, p.timestampTicks()).isEmpty()) {
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
