package io.busata.fourleft.backendeasportswrc.infrastructure.schedules;

import io.busata.fourleft.backendeasportswrc.domain.services.clubExport.ClubExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
@ConditionalOnProperty(prefix = "club-export", name = "scheduling-enabled", havingValue = "true", matchIfMissing = true)
public class ClubExportSchedule {

    private final ClubExportService clubExportService;

    @Scheduled(cron = "0 0 */1 * * *", zone = "UTC")
    public void exportEnabledClubs() {
        log.info("Starting scheduled export of enabled clubs");
        clubExportService.exportAllEnabledClubs();
    }
}
