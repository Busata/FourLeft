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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Covers the deltas the standings post renders next to each custom-scored row. They are derived by
 * replaying every finished event in order, so both "previous" values have to end up describing the
 * state before the last event — including for drivers who debuted in it or sat it out.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClubResultsServiceStandingsDeltaTest {

    private static final String CLUB_ID = "club-1";

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
        configuration.setScoringTable(Map.of("1", 10, "2", 8, "3", 6));

        when(profileService.getProfileById(anyString())).thenReturn(Optional.empty());
    }

    /** One single-stage event per board, in running order. */
    @SafeVarargs
    private void givenEvents(List<ClubLeaderboardEntry>... boards) {
        ZonedDateTime now = ZonedDateTime.now();
        List<Event> events = new ArrayList<>();

        for (int index = 0; index < boards.length; index++) {
            String boardId = "board-" + index;
            Event event = new Event("event-" + index, boardId,
                    now.minusDays(20L - index), now.minusDays(10L - index), 2L, new EventSettings());
            event.updateStages(List.of(new Stage("stage-" + index, boardId, null)));
            events.add(event);

            ClubLeaderboard board = new ClubLeaderboard(boardId, boards[index].size());
            board.updateEntries(boards[index]);
            when(clubLeaderboardService.findById(boardId)).thenReturn(board);
        }

        Championship championship = new Championship("champ-1", new ChampionshipSettings(), now.minusDays(30), now.plusDays(10));
        championship.updateEvents(events);

        Club club = new Club(CLUB_ID, "Test club", "", 10L, now.minusYears(1));
        club.updateChampionship(championship);

        when(clubService.findById(CLUB_ID)).thenReturn(club);
    }

    private static ClubLeaderboardEntry entry(String name, long rankAccumulated) {
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
                .timePenalty(Duration.ZERO)
                .build();
    }

    private ChampionshipStanding standingOf(List<ChampionshipStanding> standings, String ssid) {
        return standings.stream()
                .filter(standing -> standing.getSsid().equals(ssid))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void debutantsShowTheirFirstEventAsAGainAndCarryNoPreviousRank() {
        givenEvents(
                List.of(entry("regular", 1), entry("other", 2)),
                List.of(entry("regular", 1), entry("other", 2)),
                List.of(entry("regular", 1), entry("other", 2), entry("debutant", 3)));

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        ChampionshipStanding debutant = standingOf(standings, "debutant");
        assertThat(debutant.getPointsAccumulated()).isEqualTo(6);
        // The 6 points are new, so they read as a gain rather than the (+0) a self-seeded previous gave.
        assertThat(debutant.getPointsDifference()).isEqualTo(6);
        // Never ranked before, so the post shows (new) instead of a drop from the 0 placeholder.
        assertThat(debutant.isNewEntry()).isTrue();
    }

    @Test
    void driversWhoSitAnEventOutShowNoGain() {
        givenEvents(
                List.of(entry("regular", 1), entry("absentee", 2)),
                List.of(entry("regular", 1)));

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        ChampionshipStanding absentee = standingOf(standings, "absentee");
        assertThat(absentee.getPointsAccumulated()).isEqualTo(8);
        // Their 8 points came from the first event, not this one.
        assertThat(absentee.getPointsDifference()).isZero();
        assertThat(absentee.isNewEntry()).isFalse();
    }

    @Test
    void returningDriversShowOnlyTheLastEventsGain() {
        givenEvents(
                List.of(entry("regular", 1), entry("other", 2)),
                List.of(entry("regular", 1)),
                List.of(entry("regular", 1), entry("other", 2)));

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        ChampionshipStanding other = standingOf(standings, "other");
        assertThat(other.getPointsAccumulated()).isEqualTo(16);
        assertThat(other.getPointsDifference()).isEqualTo(8);

        ChampionshipStanding regular = standingOf(standings, "regular");
        assertThat(regular.getPointsAccumulated()).isEqualTo(30);
        assertThat(regular.getPointsDifference()).isEqualTo(10);
        assertThat(regular.getRankDifference()).isZero();
    }
}
