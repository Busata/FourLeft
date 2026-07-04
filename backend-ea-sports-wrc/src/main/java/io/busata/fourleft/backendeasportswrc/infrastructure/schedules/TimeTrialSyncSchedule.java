package io.busata.fourleft.backendeasportswrc.infrastructure.schedules;

import io.busata.fourleft.backendeasportswrc.application.timetrial.TimeTrialSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps stored time-trial boards fresh. Runs hourly but only enqueues boards whose last fetch is older
 * than {@code time-trial-sync.refresh-interval-hours} (48h), so the ~1/48 of boards due each hour drip
 * out on their own — no thundering herd, and a run missed over a restart self-heals on the next tick.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
@ConditionalOnProperty(prefix = "time-trial-sync", name = "sweep-enabled", havingValue = "true", matchIfMissing = true)
public class TimeTrialSyncSchedule {

    private final TimeTrialSyncService timeTrialSyncService;

    @Scheduled(cron = "0 0 */1 * * *", zone = "UTC")
    public void refreshStaleBoards() {
        int enqueued = timeTrialSyncService.enqueueScheduledRefresh();
        if (enqueued > 0) {
            log.info("Scheduled TT refresh enqueued {} board fetch(es)", enqueued);
        }
    }
}
