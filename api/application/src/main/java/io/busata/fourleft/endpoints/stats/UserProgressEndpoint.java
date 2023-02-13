package io.busata.fourleft.endpoints.stats;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.overview.ClubResultSummaryTo;
import io.busata.fourleft.api.models.overview.CommunityResultSummaryTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.domain.challenges.models.CommunityChallenge;
import io.busata.fourleft.domain.challenges.models.CommunityEvent;
import io.busata.fourleft.domain.challenges.repository.CommunityChallengeRepository;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.domain.progress.CommunityDailyEntries;
import io.busata.fourleft.domain.progress.CommunityDailyEntriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserProgressEndpoint {

    private final CommunityDailyEntriesRepository communityDailyEntriesRepository;
    private final ClubRepository clubRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final CommunityChallengeRepository challengeRepository;


    @GetMapping(Routes.USER_OVERVIEW)
    public UserResultSummaryTo getUserOverview(@RequestParam String query) {
        final var activeClubEvents = clubRepository.findAll().stream().flatMap(club -> club.getCurrentEvent().stream()).toList();
        final var previousClubEvents = clubRepository.findAll().stream().flatMap(club -> club.getPreviousEvent().stream()).toList();

        final var activeClubResults = getClubResults(query, activeClubEvents);
        final var previousClubResults = getClubResults(query, previousClubEvents);

        final var activeCommunityEvents = challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault())))
                .collect(Collectors.toList());

        final var activeCommunityResults = extracted(query, activeCommunityEvents);

        final var previousResults = challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault()).minusDays(1)))
                .collect(Collectors.toList());

        final var previousCommunityResults = extracted(query, previousResults);


        return new UserResultSummaryTo(
                activeCommunityResults,
                previousCommunityResults,
                activeClubResults,
                previousClubResults
        );
    }

    private List<CommunityResultSummaryTo> extracted(String query, List<CommunityChallenge> whatsthis) {
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

    private List<ClubResultSummaryTo> getClubResults(String query, List<Event> activeClubEvents) {
        return activeClubEvents.stream().flatMap(evt -> {
            return leaderboardRepository.findLeaderboardByChallengeIdAndEventIdAndStageId(evt.getChallengeId(), evt.getReferenceId(), String.valueOf(evt.getLastStage().getReferenceId()))
                    .stream().flatMap(board -> {
                        List<BoardEntry> entries = board.getEntries();
                        return entries.stream().filter(entry -> entry.getName().equalsIgnoreCase(query))
                                .findFirst().stream().map(entry -> {

                                    final var championship = evt.getChampionship();
                                    final var club = championship.getClub();


                                    return new ClubResultSummaryTo(
                                            club.getName(),
                                            evt.getCountry(),
                                            evt.getLastStage().getName(),
                                            evt.getEndTime(),
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
                    });
        }).toList();
    }
}
