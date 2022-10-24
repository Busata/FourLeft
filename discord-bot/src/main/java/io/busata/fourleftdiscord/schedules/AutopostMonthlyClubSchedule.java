package io.busata.fourleftdiscord.schedules;

import io.busata.fourleftdiscord.autoposting.automated_championships.AutoPosterAutomatedMonthlyClubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="autoposting", havingValue="true", matchIfMissing = true)
public class AutopostMonthlyClubSchedule {
    private final AutoPosterAutomatedMonthlyClubService autoPosterAutomatedMonthlyClubService;


    @Scheduled(cron = "0 10 8 * * 1", zone="Europe/Brussels")
    public void postAutomatedDailyClubResults() {
        log.info("Posting Weekly results");
        autoPosterAutomatedMonthlyClubService.postResults();
    }

    @Scheduled(cron = "5 10 8 * * 1", zone="Europe/Brussels")
    public void postAutomatedDailyChampionshipResults() {
        log.info("Posting Weekly championship results");
        autoPosterAutomatedMonthlyClubService.postChampionship();
    }

    @Scheduled(cron = "10 10 8 * * 1", zone="Europe/Brussels")
    public void postDailyChallengeInfo() {
        log.info("Posting Weekly new stage info");
        autoPosterAutomatedMonthlyClubService.postNewStage();
    }
}
