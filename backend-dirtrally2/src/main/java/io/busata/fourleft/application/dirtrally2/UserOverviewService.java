package io.busata.fourleft.application.dirtrally2;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.overview.ClubResultSummaryTo;
import io.busata.fourleft.api.models.overview.CommunityResultSummaryTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.application.dirtrally2.aggregators.ViewResultToFactory;
import io.busata.fourleft.domain.aggregators.ClubEventSupplier;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.domain.dirtrally2.community.CommunityChallenge;
import io.busata.fourleft.domain.dirtrally2.community.CommunityEvent;
import io.busata.fourleft.domain.dirtrally2.community.CommunityChallengeRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOverviewService {

    private final LeaderboardRepository leaderboardRepository;
    private final CommunityChallengeRepository challengeRepository;

    private final ViewResultToFactory viewResultToFactory;

    private final ClubViewRepository clubViewRepository;
    public UserResultSummaryTo getUserOverview(String query) {

        List<ViewResultTo> currentViewResults = getViewResults(ClubEventSupplier.CURRENT);
        List<ViewResultTo> previousViewResults = getViewResults(ClubEventSupplier.PREVIOUS);


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

    private List<ViewResultTo> getViewResults(ClubEventSupplier supplierType) {
        return clubViewRepository.findAll().stream().parallel()
                .flatMap(clubView -> viewResultToFactory.createViewResult(clubView.getId(), supplierType).stream())
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
            List<DriverEntryTo> resultEntries = viewResult.getResultEntries();

            return resultEntries.stream().filter(entry -> entry.racenet().equalsIgnoreCase(query))
                    .findFirst().stream().map(entry -> {
                        ActivityInfoTo activityInfoTo = viewResult.getEventInfo().stream().findFirst().orElseThrow();

                        final var resultList = viewResult.getMultiListResults().stream().findFirst().orElseThrow();

                        return new ClubResultSummaryTo(
                                viewResult.getDescription(),
                                activityInfoTo.country(),
                                activityInfoTo.stageNames().get(activityInfoTo.stageNames().size() - 1),
                                activityInfoTo.endTime(),
                                entry.nationality(),
                                entry.vehicles().get(0).vehicleName(),
                                entry.activityRank(),
                                resultList.totalUniqueEntries(),
                                ((float) entry.activityRank() / (float) resultList.totalUniqueEntries()) * 100f,
                                entry.isDnf(),
                                entry.activityTotalTime(),
                                entry.activityTotalDiff()
                        );
                    });
        }).collect(Collectors.toList());
    }
}
