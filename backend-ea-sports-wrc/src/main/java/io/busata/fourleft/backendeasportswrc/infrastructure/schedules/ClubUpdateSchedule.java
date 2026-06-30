package io.busata.fourleft.backendeasportswrc.infrastructure.schedules;

import io.busata.fourleft.backendeasportswrc.application.importer.ClubsImporterService;
import io.busata.fourleft.backendeasportswrc.application.work.queue.QueueProperties;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class ClubUpdateSchedule {

    private final ClubConfigurationService clubConfigurationService;
    private final ClubsImporterService importerService;
    private final QueueProperties queueProperties;

    @Scheduled(cron = "*/5 * * * * *", zone = "UTC")
    public void updateImporter() {
        // When the new worker queue is enabled it owns club imports; stand down to
        // avoid both systems importing every club at once.
        if (queueProperties.isEnabled()) {
            return;
        }
        importerService.sync();
    }

    @Scheduled(cron = "0 0 0/6 * * *", zone = "UTC")
    public void resetDisabledClubs() {
        clubConfigurationService.resetDisabledClubs();
    }

}
