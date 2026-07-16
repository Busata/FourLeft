package io.busata.fourleft.backendacrally.application.ingest;

import io.busata.fourleft.backendacrally.domain.models.session.AgentSession;
import io.busata.fourleft.backendacrally.domain.models.session.StageResult;
import io.busata.fourleft.backendacrally.domain.services.championship.EventRecordingService;
import io.busata.fourleft.backendacrally.domain.services.session.AgentSessionRepository;
import io.busata.fourleft.backendacrally.domain.services.session.StageResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ingestion guards. Pure Mockito — no Spring context or DB needed. */
@ExtendWith(MockitoExtension.class)
class SessionIngestServiceTest {

    @Mock AgentSessionRepository sessions;
    @Mock StageResultRepository results;
    @Mock EventRecordingService recordingService;
    @Mock AgentVersionPolicy versionPolicy;

    @InjectMocks SessionIngestService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    private IngestPayloads.Result resultWithTicks(long ticks) {
        return new IngestPayloads.Result(
                "GreeceS3ElatiaCut1Reverse", "CitroenXsaraWRC", "Busata",
                240_282, 0, 240_282, ticks, null, null, "0.3.6");
    }

    private IngestPayloads.SessionStart sessionStart(Boolean recovery) {
        return new IngestPayloads.SessionStart(
                "Busata", "MiniCooperS1275", "WelesS4HafrenSouthFullForward",
                "WelesS4HafrenSouthFullForward", null, null, 0L, "0.3.8", recovery);
    }

    private AgentSession ownedSession() {
        AgentSession session = new AgentSession(
                userId, UUID.randomUUID(), "Busata", "CitroenXsaraWRC",
                "GreeceS3ElatiaCut1Reverse", "GreeceS3ElatiaCut1Reverse", 0L, "0.3.6");
        when(sessions.findById(sessionId)).thenReturn(Optional.of(session));
        return session;
    }

    // 2026-07-09 in prod: a false save anchor decoding to year-2057 ticks was submitted
    // (stage name in the car field) and poisoned the agent's result floor.
    @Test
    void rejectsResultStampedInTheFuture() {
        ownedSession();

        assertThatThrownBy(() -> service.recordResult(
                userId, sessionId.toString(), resultWithTicks(649_081_296_294_772_799L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("future");

        verify(results, never()).save(any());
    }

    // 2026-07-15 in prod: the first startup-recovery session bound the user's armed arm,
    // its replayed (duplicate) result skipped recordIfArmed, and the arm stayed BOUND to a
    // dead session forever — the real armed run then passed by unscored.
    @Test
    void recoverySessionsNeverBindAnArm() {
        when(sessions.save(any())).thenAnswer(inv -> inv.getArgument(0, AgentSession.class));

        service.open(userId, UUID.randomUUID(), sessionStart(true));

        verify(recordingService, never()).bindToSession(any(), any());
    }

    @Test
    void liveSessionsStillBindAnArm() {
        when(sessions.save(any())).thenAnswer(inv -> inv.getArgument(0, AgentSession.class));

        service.open(userId, UUID.randomUUID(), sessionStart(null));

        verify(recordingService).bindToSession(eq(userId), any());
    }

    @Test
    void outdatedAgentIsRejectedBeforeAnythingIsStored() {
        org.mockito.Mockito.doThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.UPGRADE_REQUIRED, "too old"))
                .when(versionPolicy).requireSupported("0.3.6");

        assertThatThrownBy(() -> service.recordResult(
                userId, sessionId.toString(), resultWithTicks(639_191_141_860_160_000L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("too old");

        verify(results, never()).save(any());
        verify(sessions, never()).findById(any());
    }

    @Test
    void duplicateResultResolvesABoundArmAsDnf() {
        // A replayed result can never score, and releasing the arm instead would let a crafted
        // duplicate post act as a retry button — the bound run resolves as DNF.
        AgentSession session = ownedSession();
        long ticks = 639_191_141_860_160_000L;
        when(results.findByUserIdAndTimestampTicksAndTotalMs(userId, ticks, 240_282))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(StageResult.class)));

        service.recordResult(userId, sessionId.toString(), resultWithTicks(ticks));

        verify(results, never()).save(any());
        verify(recordingService).dnfSession(session.getId());
    }

    @Test
    void acceptsResultWithPlausibleTimestamp() {
        ownedSession();
        long ticksJuly2026 = 639_191_141_860_160_000L;
        when(results.findByUserIdAndTimestampTicksAndTotalMs(userId, ticksJuly2026, 240_282))
                .thenReturn(Optional.empty());
        when(results.save(any())).thenAnswer(inv -> inv.getArgument(0, StageResult.class));

        assertThatCode(() -> service.recordResult(
                userId, sessionId.toString(), resultWithTicks(ticksJuly2026)))
                .doesNotThrowAnyException();

        verify(results).save(any());
    }
}
