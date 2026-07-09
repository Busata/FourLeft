package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboard;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.common.ScoringStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Exercises RACENET_DEFAULT through the real custom-scoring path: the field size racenet's formula
 * scales with counts everyone who started the event, so mid-event dropouts (present on earlier stage
 * boards only) both widen the curve and collect its tail — ranked by furthest stage reached, then
 * accumulated time. A 10-strong field scores {10, 7, 6, 5, 4, 2, 1, 0, 0, 0}, straight from racenet's
 * own observed standings.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClubResultsServiceRacenetDefaultTest {

    private static final String CLUB_ID = "club-1";
    private static final String STAGE_1_BOARD = "board-stage-1";
    private static final String STAGE_2_BOARD = "board-stage-2";
    private static final String FINAL_BOARD = "board-stage-3";

    @Mock ClubService clubService;
    @Mock ClubLeaderboardService clubLeaderboardService;
    @Mock ProfileService profileService;

    private ClubResultsService clubResultsService;
    private DiscordClubConfiguration configuration;

    @BeforeEach
    void setUp() {
        clubResultsService = new ClubResultsService(
                clubService, clubLeaderboardService, profileService,
                new ScoringService(), new RestrictionService());

        configuration = new DiscordClubConfiguration(0L, 0L, CLUB_ID, true);
        configuration.setCustomScoringEnabled(true);
        configuration.setScoringStrategy(ScoringStrategy.RACENET_DEFAULT);

        when(profileService.getProfileById(anyString())).thenReturn(Optional.empty());
    }

    private void givenThreeStageEvent(List<ClubLeaderboardEntry> stage1, List<ClubLeaderboardEntry> stage2,
                                      List<ClubLeaderboardEntry> finishers) {
        ZonedDateTime now = ZonedDateTime.now();

        Event event = new Event("event-1", FINAL_BOARD, now.minusDays(5), now.minusDays(1), 2L, new EventSettings());
        event.updateStages(List.of(
                new Stage("stage-1", STAGE_1_BOARD, null),
                new Stage("stage-2", STAGE_2_BOARD, null),
                new Stage("stage-3", FINAL_BOARD, null)));

        Championship championship = new Championship("champ-1", new ChampionshipSettings(), now.minusDays(10), now.plusDays(10));
        championship.updateEvents(List.of(event));

        Club club = new Club(CLUB_ID, "Test club", "", 10L, now.minusYears(1));
        club.updateChampionship(championship);

        ClubLeaderboard board = new ClubLeaderboard(FINAL_BOARD, finishers.size());
        board.updateEntries(finishers);

        when(clubService.findById(CLUB_ID)).thenReturn(club);
        when(clubLeaderboardService.findById(FINAL_BOARD)).thenReturn(board);
        when(clubLeaderboardService.findEntriesByLeaderboardIds(List.of(STAGE_1_BOARD, STAGE_2_BOARD)))
                .thenReturn(Map.of(STAGE_1_BOARD, stage1, STAGE_2_BOARD, stage2));
    }

    private static ClubLeaderboardEntry entry(String name, long rankAccumulated, boolean dnf) {
        Duration time = Duration.ofMinutes(3).plusSeconds(rankAccumulated);
        return ClubLeaderboardEntry.builder()
                .displayName(name)
                .ssid(name)
                .rank(rankAccumulated)
                .rankAccumulated(rankAccumulated)
                .nationalityID(1L)
                .vehicle("Audi Sport quattro S1 E2")
                .time(time)
                .timeAccumulated(time)
                // isDnf() is time == timePenalty, so a DNF carries its full time as penalty.
                .timePenalty(dnf ? time : Duration.ZERO)
                .build();
    }

    private int pointsOf(List<ChampionshipStanding> standings, String ssid) {
        return standings.stream()
                .filter(standing -> standing.getSsid().equals(ssid))
                .findFirst()
                .orElseThrow()
                .getPointsAccumulated();
    }

    @Test
    void scalesWithStartersAndAwardsDropoutsTheTail() {
        List<ClubLeaderboardEntry> finishers = List.of(
                entry("first", 1, false),
                entry("second", 2, false),
                entry("third", 3, false),
                entry("fourth", 4, false),
                entry("fifth", 5, false),
                entry("sixth", 6, false));
        // Both stage-2 dropouts got further than the stage-1 ones, so they rank 7 and 8; within a
        // stage the faster accumulated time wins the better slot.
        List<ClubLeaderboardEntry> stage2 = List.of(
                entry("first", 1, false), entry("second", 2, false), entry("third", 3, false),
                entry("fourth", 4, false), entry("fifth", 5, false), entry("sixth", 6, false),
                entry("drop-s2-fast", 7, false),
                entry("drop-s2-slow", 8, false));
        List<ClubLeaderboardEntry> stage1 = List.of(
                entry("first", 1, false), entry("second", 2, false), entry("third", 3, false),
                entry("fourth", 4, false), entry("fifth", 5, false), entry("sixth", 6, false),
                entry("drop-s2-fast", 7, false), entry("drop-s2-slow", 8, false),
                entry("drop-s1", 9, false),
                entry("drop-s1-slow", 10, false));

        givenThreeStageEvent(stage1, stage2, finishers);

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        // Field of 10 starters: 10, 7, 6, 5, 4, 2 for the finishers even though only 6 reached the end...
        assertThat(pointsOf(standings, "first")).isEqualTo(10);
        assertThat(pointsOf(standings, "second")).isEqualTo(7);
        assertThat(pointsOf(standings, "third")).isEqualTo(6);
        assertThat(pointsOf(standings, "fourth")).isEqualTo(5);
        assertThat(pointsOf(standings, "fifth")).isEqualTo(4);
        assertThat(pointsOf(standings, "sixth")).isEqualTo(2);
        // ...and the dropouts take the curve's tail in dropout order (positions 7-10 score 1, 0, 0, 0).
        assertThat(pointsOf(standings, "drop-s2-fast")).isEqualTo(1);
        assertThat(pointsOf(standings, "drop-s2-slow")).isZero();
        assertThat(pointsOf(standings, "drop-s1")).isZero();
        assertThat(pointsOf(standings, "drop-s1-slow")).isZero();
    }

    @Test
    void dnfOnTheFinalBoardIsRankedAndScoredLikeRacenetDoes() {
        // Racenet ranks full-penalty entries by their (penalised) time and runs them through the same
        // curve — unlike the other strategies, which skip DNFs entirely.
        List<ClubLeaderboardEntry> finishers = List.of(
                entry("first", 1, false),
                entry("dnf", 2, true),
                entry("third", 3, false),
                entry("fourth", 4, false));

        givenThreeStageEvent(finishers, finishers, finishers);

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        // Field of 4: 4, 2, 1, 0 — the DNF keeps position 2 and its 2 points.
        assertThat(pointsOf(standings, "first")).isEqualTo(4);
        assertThat(pointsOf(standings, "dnf")).isEqualTo(2);
        assertThat(pointsOf(standings, "third")).isEqualTo(1);
        assertThat(pointsOf(standings, "fourth")).isZero();
    }
}
