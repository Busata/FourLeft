package io.busata.fourleft.endpoints.stats;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.overview.ClubResultSummaryTo;
import io.busata.fourleft.api.models.overview.CommunityResultSummaryTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.challenges.models.CommunityChallenge;
import io.busata.fourleft.domain.challenges.models.CommunityEvent;
import io.busata.fourleft.domain.challenges.repository.CommunityChallengeRepository;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.endpoints.views.ClubEventSupplier;
import io.busata.fourleft.endpoints.views.ViewResultToFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserProgressEndpoint {

    private final LeaderboardRepository leaderboardRepository;
    private final CommunityChallengeRepository challengeRepository;

    private final ViewResultToFactory viewResultToFactory;

    private final ClubViewRepository clubViewRepository;


    @GetMapping(Routes.USER_OVERVIEW)
    public UserResultSummaryTo getUserOverview(@RequestParam String query) {

        List<ViewResultTo> currentViewResults = getViewResults(Club::getCurrentEvent);
        List<ViewResultTo> previousViewResults = getViewResults(Club::getPreviousEvent);


        final var activeClubResults = getClubResults(query, currentViewResults);
        final var previousClubResults = getClubResults(query, previousViewResults);

        final var activeCommunityEvents = challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault())))
                .collect(Collectors.toList());

        final var activeCommunityResults = createCommunityResults(query, activeCommunityEvents);

        final var previousResults = challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault()).minusDays(1)))
                .collect(Collectors.toList());

        final var previousCommunityResults = createCommunityResults(query, previousResults);


        return new UserResultSummaryTo(
                activeCommunityResults,
                previousCommunityResults,
                activeClubResults,
                previousClubResults
        );
    }

    private List<ViewResultTo> getViewResults(ClubEventSupplier supplier) {
        return clubViewRepository.findAll().stream().
                flatMap(clubView -> viewResultToFactory.createViewResult(clubView.getId(), supplier).stream())
                .collect(Collectors.toList());
    }

    private List<CommunityResultSummaryTo> createCommunityResults(String query, List<CommunityChallenge> whatsthis) {
        return whatsthis.stream().flatMap(communityChallenge ->
                communityChallenge.getLeaderboardKey().stream()
                .map(leaderboardRepository::findLeaderboard)
                .flatMap(Optional::stream)
                .flatMap(board -> {
                    List<BoardEntry> entries = board.getEntries();
                    return entries.stream().filter(entry -> entry.getName().equalsIgnoreCase(query))
                            .findFirst().stream().map(entry -> {

                                final var vehicleClass = communityChallenge.getVehicleClass();
                                final var stageName = communityChallenge.getLastEvent().map(CommunityEvent::getName).orElse("");
                                final var endTime = communityChallenge.getEndTime();

                                return new CommunityResultSummaryTo(
                                        stageName,
                                        vehicleClass,
                                        endTime,
                                        entry.getNationality(),
                                        entry.getVehicleName(),
                                        entry.getRank(),
                                        entries.size(),
                                        ((float) entry.getRank() / (float) entries.size()) * 100f,
                                        entry.isDnf(),
                                        entry.getTotalTime(),
                                        entry.getTotalDiff()
                                );
                            });

                })).collect(Collectors.toList());
    }

    private List<ClubResultSummaryTo> getClubResults(String query, List<ViewResultTo> viewResults) {

        return viewResults.stream().flatMap(viewResult -> {
            List<ResultEntryTo> resultEntries = viewResult.getResultEntries();

            return resultEntries.stream().filter(entry -> entry.name().equalsIgnoreCase(query))
                    .findFirst().stream().map(entry -> {
                        EventInfoTo eventInfoTo = viewResult.getEventInfo().stream().findFirst().orElseThrow();

                        return new ClubResultSummaryTo(
                                viewResult.getDescription(),
                                eventInfoTo.country(),
                                eventInfoTo.stageNames().get(eventInfoTo.stageNames().size() - 1),
                                eventInfoTo.endTime(),
                                entry.nationality(),
                                entry.vehicle(),
                                entry.rank(),
                                resultEntries.size(),
                                ((float) entry.rank() / (float) resultEntries.size()) * 100f,
                                entry.isDnf(),
                                entry.totalTime(),
                                entry.totalDiff()
                        );
                    });
        }).collect(Collectors.toList());
    }
}
