package io.busata.fourleft.schedules;

import io.busata.fourleft.importer.updaters.RacenetChallengesSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="community", havingValue="true", matchIfMissing = true)
public class CommunityChallengeUpdaterSchedule {

    private final RacenetChallengesSyncService updater;

    @PostConstruct
    public void init() {
        log.info("COMMUNITY CHALLENGES SCHEDULE -- ACTIVE");
    }

    @Scheduled(initialDelay = 15000, fixedDelay=Long.MAX_VALUE)
    public void updateAfterStartup() {
        log.info("Startup update of community challenges");
        updater.syncWithRacenet();
    }

    @Scheduled(cron = "0 15 11 * * *", zone="Europe/Brussels")
    public void updateChallenges() {
        log.info("Updating community challenges");
        updater.syncWithRacenet();
    }
}
