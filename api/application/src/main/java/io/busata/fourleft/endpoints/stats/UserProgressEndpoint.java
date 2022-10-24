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

    @GetMapping(Routes.USER_PROGRESS_DAILY)
    public ResponseEntity<byte[]> getProgress(@RequestParam String userName) throws IOException {

        final var entries = communityDailyEntriesRepository.findByName(userName);
        log.info("Entries: {} ", entries.size());

        return createResponseEntity(start(userName, entries));

    }

    public BufferedImage start(String userName, List<CommunityDailyEntries> entries) {

        XYDataset dataset = createDataset(entries);
        JFreeChart chart = createChart(dataset, userName);


        return chart.createBufferedImage(1920, 400);
    }

    private ResponseEntity<byte[]> createResponseEntity(BufferedImage image) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(bos.size())
                .body(bos.toByteArray());
    }

    private XYDataset createDataset(List<CommunityDailyEntries> entries) {
        List<CommunityDailyEntries> communityDailyEntries = entries.stream()
                .filter(entry -> !entry.isDnf())
                .sorted(Comparator.comparing(CommunityDailyEntries::getEndTime)).toList();
        var series = new XYSeries("Progress");

        IntStream.range(0, communityDailyEntries.size()).forEach(i -> {

            var entry = communityDailyEntries.get(i);
            series.add(i + 1, ((float) entry.getRank() / (float) entry.getTotalRank()) * 100f);
        });

        var dataset = new XYSeriesCollection();

        dataset.addSeries(series);

        return dataset;
    }

    private JFreeChart createChart(XYDataset dataset, String userName) {

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Progress",
                "Day",
                "Rank %",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        var renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);


        chart.getLegend().setFrame(BlockBorder.NONE);

        chart.setTitle(new TextTitle("Daily Rank % - " + userName,
                        new Font("Serif", java.awt.Font.BOLD, 18)
                )
        );

        return chart;
    }

}
