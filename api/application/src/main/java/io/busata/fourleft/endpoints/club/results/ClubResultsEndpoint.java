package io.busata.fourleft.endpoints.club.results;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import io.busata.fourleft.api.models.ChampionshipStageSummaryTo;
import io.busata.fourleft.api.models.ChampionshipStandingEntryTo;
import io.busata.fourleft.api.models.ClubResultTo;
import io.busata.fourleft.api.models.CustomChampionshipStandingEntryTo;
import io.busata.fourleft.endpoints.club.results.service.ClubResultToFactory;
import io.busata.fourleft.endpoints.club.results.service.CustomChampionshipStandingsService;
import io.busata.fourleft.importer.ClubSyncService;
import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.importer.updaters.RacenetSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClubResultsEndpoint {
    private final ClubSyncService clubSyncService;

    private final RacenetSyncService racenetSyncService;
    private final ClubResultToFactory clubResultToFactory;
    private final CustomChampionshipStandingsService championshipStandingsService;

    @PostMapping(Routes.REFRESH_CLUB_BY_CLUB_ID)
    public void forceRefresh(@PathVariable Long clubId) {
        Club club = clubSyncService.getOrCreate(clubId);

        racenetSyncService.fullRefreshClub(club.getReferenceId());
    }

    @GetMapping(Routes.CLUB_EVENT_SUMMARY_BY_CLUB_ID)
    public ChampionshipEventSummaryTo getEventSummary(@PathVariable Long clubId) {
        final var club = clubSyncService.getOrCreate(clubId);
        return club.findActiveChampionship().or(club::findPreviousChampionship)
                .map(clubResultToFactory::createEventSummary)
                .orElseThrow();
    }

    @GetMapping(Routes.CLUB_STANDINGS_BY_CLUB_ID)
    public List<ChampionshipStandingEntryTo> getChampionshipStandings(@PathVariable Long clubId) {
        Club club = clubSyncService.getOrCreate(clubId);
        return club.findActiveChampionship()
                .or(club::findPreviousChampionship)
                .map(Championship::getEntries)
                .map(clubResultToFactory::createStandingsSummary)
                .orElseThrow();
    }

    @GetMapping(Routes.CLUB_CUSTOM_STANDINGS_BY_CLUB_ID)
    public List<CustomChampionshipStandingEntryTo> getCustomStandings(@PathVariable Long clubId, @RequestParam(defaultValue = "4") int lastXChampionships, @RequestParam(defaultValue="true") boolean powerStageSystem) {
        return championshipStandingsService.getEntries(clubId, lastXChampionships, powerStageSystem);
    }


    @GetMapping(Routes.STAGE_SUMMARY_BY_CLUB_ID)
    public ChampionshipStageSummaryTo getStageSummary(@PathVariable Long clubId) {
        return clubSyncService.getOrCreate(clubId).getChampionships().stream()
                .filter(Championship::isActive)
                .findFirst()
                .map(clubResultToFactory::createStageSummary)
                .orElseThrow();
    }
}
