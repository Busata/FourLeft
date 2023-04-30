package io.busata.fourleft.infrastructure.schedules;

import io.busata.fourleft.application.dirtrally2.importer.ClubSyncUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="clubs", havingValue="true", matchIfMissing = true)
public class ClubUpdateSchedule {
    private final ClubSyncUsecase clubSyncService;

    @PostConstruct
    public void init() {
        log.info("CLUB UPDATE SCHEDULE -- ACTIVE");
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "UTC")
    @CacheEvict(value = "view_results", allEntries = true)
    public void updateLeaderboards() {
        clubSyncService.updateLeaderboards();
    }
}
