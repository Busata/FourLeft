package io.busata.fourleft.backendacrally.infrastructure.schedules;

import io.busata.fourleft.backendacrally.domain.services.championship.EventArmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Expires arms that were never followed by a run. An arm binds to the driver's NEXT session,
 * so a forgotten one would wait indefinitely and capture a casual run started days later;
 * instead it times out and resolves as DNF.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class EventArmExpirySchedule {

    /** How long an armed stage waits for its run before resolving as DNF. Long enough to
     *  survive dinner-length breaks and stale-session recovery (which re-ARMs the arm and
     *  refreshes its activity); short enough that a forgotten arm can't ambush next week's run. */
    private static final Duration EXPIRE_AFTER = Duration.ofHours(6);

    private final EventArmService armService;

    @Scheduled(cron = "0 */15 * * * *", zone = "UTC")
    public void expire() {
        int expired = armService.expireIdleArms(LocalDateTime.now().minus(EXPIRE_AFTER));
        if (expired > 0) {
            log.info("Expired {} idle arm(s) as DNF", expired);
        }
    }
}
