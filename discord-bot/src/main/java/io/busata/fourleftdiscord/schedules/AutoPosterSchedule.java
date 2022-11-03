package io.busata.fourleftdiscord.schedules;

import io.busata.fourleftdiscord.autoposting.community_challenges.AutoPostCommunityEventResultsService;
import io.busata.fourleftdiscord.autoposting.club_results.AutoPostClubResultsService;
import io.busata.fourleftdiscord.autoposting.automated_championships.AutoPosterAutomatedDailyClubService;
import io.busata.fourleftdiscord.commands.DiscordChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="autoposting", havingValue="true", matchIfMissing = true)
public class AutoPosterSchedule {
    private final AutoPostClubResultsService autoPostClubResultsService;
    private final AutoPostCommunityEventResultsService autoPostCommunityEventResultsService;
    private final AutoPosterAutomatedDailyClubService autoPosterAutomatedDailyClubService;

    @Scheduled(fixedRate = 300000, initialDelay = 10000)
    public void postAutoSchedule() {
        log.info("Checking for new club entries.");
        autoPostClubResultsService.update();
        log.info("Club entries check complete.");
    }

    @Scheduled(cron = "0 30 11 * * *", zone="Europe/Brussels")
    public void updateChallenges() {
        log.info("Checking community event results.");
        autoPostCommunityEventResultsService.update();
        log.info("Community events check complete.");
    }

    @Scheduled(cron = "0 1 10 * * *", zone="Europe/Brussels")
    public void postAutomatedDailyClubResults() {
        log.info("Posting Daily results");
    autoPosterAutomatedDailyClubService.postResults(DiscordChannels.DIRTY_DAILIES);
    autoPosterAutomatedDailyClubService.postResults(DiscordChannels.MAINTMASTER_SRD_DAILIES);
    }

    @Scheduled(cron = "55 0 10 * * 1", zone="Europe/Brussels")
    public void postAutomatedDailyChampionshipResults() {
        log.info("Posting Daily championship results");
        autoPosterAutomatedDailyClubService.postChampionship(DiscordChannels.DIRTY_DAILIES);
        autoPosterAutomatedDailyClubService.postChampionship(DiscordChannels.MAINTMASTER_SRD_DAILIES);
    }

    @Scheduled(cron = "5 1 10 * * *", zone="Europe/Brussels")
    public void postDailyChallengeInfo() {
        log.info("Posting Club new stage info");
        autoPosterAutomatedDailyClubService.postNewStage(DiscordChannels.DIRTY_DAILIES);
        autoPosterAutomatedDailyClubService.postNewStage(DiscordChannels.MAINTMASTER_SRD_DAILIES);
    }
}
