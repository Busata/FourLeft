package io.busata.fourleft.schedules;

import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="clubs", havingValue="true", matchIfMissing = true)
public class ClubUpdateSchedule {

    private final ClubSyncService clubSyncService;

    @PostConstruct
    public void init() {
        log.info("CLUB UPDATE SCHEDULE -- ACTIVE");
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Brussels")
    public void updateLeaderboards() {
        clubSyncService.updateClubs();
    }
}
