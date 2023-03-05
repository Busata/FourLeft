package io.busata.fourleft.endpoints.stats;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.overview.ClubResultSummaryTo;
import io.busata.fourleft.api.models.overview.CommunityResultSummaryTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.challenges.models.CommunityChallenge;
import io.busata.fourleft.domain.challenges.models.CommunityEvent;
import io.busata.fourleft.domain.challenges.repository.CommunityChallengeRepository;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.repository.CommunityChallengeSummaryTo;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.endpoints.views.ClubEventSupplierType;
import io.busata.fourleft.endpoints.views.results.factory.ViewResultToFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.image.BufferedImage;
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


    @GetMapping(value = Routes.USER_COMMUNITY_PROGRESSION, produces = "image/png")
    public BufferedImage calculateUser(@RequestParam String query,
                                       @RequestParam(required = false, defaultValue = "false") boolean includeName,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> before,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  Optional<LocalDate> after) {

        List<CommunityChallengeSummaryTo> communityChallengeSummary = leaderboardRepository.findCommunityChallengeSummary(query)
                .stream().filter(summary -> {
                    LocalDate challengeDate = summary.getChallengeDate();
                    boolean isBefore = before.map(challengeDate::isBefore).orElse(true);
                    boolean isAfter = after.map(challengeDate::isAfter).orElse(true);
                    return isBefore && isAfter;
                }).toList();

        int totalSize = communityChallengeSummary.size();

        int imageHeight = 50;
        BufferedImage imageOut = new BufferedImage(totalSize*2, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < communityChallengeSummary.size()*2; x++) {
            CommunityChallengeSummaryTo summaryEntry = communityChallengeSummary.get((int)x/2);

            final var percentageRank = ((float) summaryEntry.getRank() / (float) summaryEntry.getTotal()) * 100f;
            final var color = getPixelColour(percentageRank, summaryEntry.getIsDnf());

            for(int y = 0; y < imageHeight; y++) {
                imageOut.setRGB(x, y, color);
            }
        }

        if(includeName) {
            final var graphics = imageOut.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Arial", Font.BOLD, 10));
            int i = graphics.getFontMetrics().stringWidth(query) + 5;
            graphics.drawString(query, totalSize * 2 - i, 47);
            graphics.dispose();
        }

        return imageOut;

    }

    private static int getPixelColour(float percentageRank, boolean isDnf) {
        if (isDnf) {
            return Color.decode("#212529").getRGB();
        } else if (percentageRank <= 1) {
            return Color.decode("#ffc107").getRGB();
        } else if (percentageRank <= 10) {
            return Color.decode("#dc3545").getRGB();
        } else if (percentageRank <= 35) {
            return Color.decode("#198754").getRGB();
        } else if (percentageRank <= 75) {
            return Color.decode("#0d6efd").getRGB();
        } else {
            return Color.decode("#6c757d").getRGB();
        }
    }


    @GetMapping(Routes.USER_OVERVIEW)
    public UserResultSummaryTo getUserOverview(@RequestParam String query) {

        List<ViewResultTo> currentViewResults = getViewResults(ClubEventSupplierType.CURRENT);
        List<ViewResultTo> previousViewResults = getViewResults(ClubEventSupplierType.PREVIOUS);


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

    private List<ViewResultTo> getViewResults(ClubEventSupplierType supplierType) {
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
            List<ResultEntryTo> resultEntries = viewResult.getResultEntries();

            return resultEntries.stream().filter(entry -> entry.name().equalsIgnoreCase(query))
                    .findFirst().stream().map(entry -> {
                        ActivityInfoTo activityInfoTo = viewResult.getEventInfo().stream().findFirst().orElseThrow();

                        final var resultList = viewResult.getMultiListResults().stream().findFirst().orElseThrow();

                        return new ClubResultSummaryTo(
                                viewResult.getDescription(),
                                activityInfoTo.country(),
                                activityInfoTo.stageNames().get(activityInfoTo.stageNames().size() - 1),
                                activityInfoTo.endTime(),
                                entry.nationality(),
                                entry.vehicle(),
                                entry.rank(),
                                resultList.totalEntries(),
                                ((float) entry.rank() / (float) resultList.totalEntries()) * 100f,
                                entry.isDnf(),
                                entry.totalTime(),
                                entry.totalDiff()
                        );
                    });
        }).collect(Collectors.toList());
    }
}
