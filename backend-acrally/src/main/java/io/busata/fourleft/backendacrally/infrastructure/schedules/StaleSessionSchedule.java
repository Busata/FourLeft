package io.busata.fourleft.backendacrally.infrastructure.schedules;

import io.busata.fourleft.backendacrally.application.ingest.SessionIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Times out agent sessions that will never resolve. The agent's aborts are at-most-once
 * (network hiccup, crash, kill), so an OPEN session with no heartbeat for this long is
 * dead — without the sweep it would sit OPEN forever (see API_CONTRACT §Delivery).
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class StaleSessionSchedule {

    /** Longest plausible silence for a live run: heartbeats tick ~1/s while driving, and a
     *  finished run resolves (result or abort) within the agent's 60s save-wait. Half an hour
     *  comfortably covers long stages and mid-run pauses. */
    private static final Duration STALE_AFTER = Duration.ofMinutes(30);

    private final SessionIngestService ingestService;

    @Scheduled(cron = "0 */5 * * * *", zone = "UTC")
    public void sweep() {
        int swept = ingestService.sweepStaleSessions(LocalDateTime.now().minus(STALE_AFTER));
        if (swept > 0) {
            log.info("Marked {} silent open agent session(s) STALE", swept);
        }
    }
}
