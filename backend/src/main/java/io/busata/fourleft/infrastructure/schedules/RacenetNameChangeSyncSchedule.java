package io.busata.fourleft.infrastructure.schedules;

import io.busata.fourleft.application.dirtrally2.racenetsync.RacenetNameSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="racenetnames", havingValue="true", matchIfMissing = true)
public class RacenetNameChangeSyncSchedule {

    private final RacenetNameSyncService syncService;
    public void syncRacenetNames() {
        syncService.sync();
    }
}
