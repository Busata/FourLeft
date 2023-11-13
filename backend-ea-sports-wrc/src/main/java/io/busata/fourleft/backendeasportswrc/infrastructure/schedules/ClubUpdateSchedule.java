package io.busata.fourleft.backendeasportswrc.infrastructure.schedules;

import io.busata.fourleft.backendeasportswrc.application.importer.ClubsImporterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class ClubUpdateSchedule {

    private final ClubsImporterService importerService;

    @Scheduled(cron = "*/5 * * * * *", zone = "UTC")
    public void updateImporter() {
        importerService.sync();
    }

}
