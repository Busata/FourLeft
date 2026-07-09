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
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.common.RestrictionDisplayMode;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.RestrictionType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Exercises the restriction hooks in the custom-scoring path through the real {@link ScoringService}
 * and {@link RestrictionService} (only the data-access collaborators are mocked): scoring-EXCLUDE
 * promotes compliant drivers into the vacated positions, PENALTY deducts a flat amount floored at 0,
 * and the existing DNF handling is untouched.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClubResultsServiceRestrictionTest {

    private static final String CLUB_ID = "club-1";
    private static final String EVENT_ID = "event-1";
    private static final String STAGE_BOARD_ID = "board-stage-1";

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

    private void givenBoard(ClubLeaderboardEntry... entries) {
        ZonedDateTime now = ZonedDateTime.now();

        Event event = new Event(EVENT_ID, "board-event-1", now.minusDays(5), now.minusDays(1), 2L, new EventSettings());
        event.updateStages(List.of(new Stage("stage-1", STAGE_BOARD_ID, null)));

        Championship championship = new Championship("champ-1", new ChampionshipSettings(), now.minusDays(10), now.plusDays(10));
        championship.updateEvents(List.of(event));

        Club club = new Club(CLUB_ID, "Test club", "", 10L, now.minusYears(1));
        club.updateChampionship(championship);

        ClubLeaderboard board = new ClubLeaderboard(STAGE_BOARD_ID, entries.length);
        board.updateEntries(List.of(entries));

        when(clubService.findById(CLUB_ID)).thenReturn(club);
        when(clubLeaderboardService.findById(STAGE_BOARD_ID)).thenReturn(board);
    }

    private void givenRestriction(RestrictionScoringMode scoringMode, Integer penaltyPoints, String... allowedVehicles) {
        configuration.setEventRestrictions(List.of(new EventRestriction(
                RestrictionType.VEHICLE_ALLOWLIST, null, EVENT_ID,
                RestrictionDisplayMode.WARN, scoringMode, penaltyPoints, List.of(allowedVehicles))));
    }

    private static ClubLeaderboardEntry entry(String name, long rankAccumulated, String vehicle, boolean dnf) {
        Duration time = Duration.ofMinutes(3).plusSeconds(rankAccumulated);
        return ClubLeaderboardEntry.builder()
                .displayName(name)
                .ssid(name)
                .rank(rankAccumulated)
                .rankAccumulated(rankAccumulated)
                .nationalityID(1L)
                .vehicle(vehicle)
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
    void excludeModeZeroesViolatorAndPromotesCompliantDrivers() {
        givenBoard(
                entry("violator", 1, "Lancia Delta S4", false),
                entry("second", 2, "Audi Sport quattro S1 E2", false),
                entry("third", 3, "Audi Sport quattro S1 E2", false));
        givenRestriction(RestrictionScoringMode.EXCLUDE, null, "Audi Sport quattro S1 E2");

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        assertThat(pointsOf(standings, "violator")).isZero();
        assertThat(pointsOf(standings, "second")).isEqualTo(10);
        assertThat(pointsOf(standings, "third")).isEqualTo(8);
    }

    @Test
    void penaltyModeDeductsFlatPointsKeepingPositions() {
        givenBoard(
                entry("violator", 1, "Lancia Delta S4", false),
                entry("second", 2, "Audi Sport quattro S1 E2", false));
        givenRestriction(RestrictionScoringMode.PENALTY, 4, "Audi Sport quattro S1 E2");

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        assertThat(pointsOf(standings, "violator")).isEqualTo(6);
        assertThat(pointsOf(standings, "second")).isEqualTo(8);
    }

    @Test
    void penaltyNeverDropsBelowZero() {
        givenBoard(
                entry("violator", 1, "Lancia Delta S4", false),
                entry("second", 2, "Audi Sport quattro S1 E2", false));
        givenRestriction(RestrictionScoringMode.PENALTY, 15, "Audi Sport quattro S1 E2");

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        assertThat(pointsOf(standings, "violator")).isZero();
    }

    @Test
    void compliantDnfStillScoresZeroAndKeepsItsSlot() {
        givenBoard(
                entry("violator", 1, "Lancia Delta S4", false),
                entry("dnf", 2, "Audi Sport quattro S1 E2", true),
                entry("third", 3, "Audi Sport quattro S1 E2", false));
        givenRestriction(RestrictionScoringMode.EXCLUDE, null, "Audi Sport quattro S1 E2");

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        assertThat(pointsOf(standings, "dnf")).isZero();
        // The DNF occupies scoring position 1, so the finisher behind it scores position 2, not 1.
        assertThat(pointsOf(standings, "third")).isEqualTo(8);
    }

    @Test
    void withoutARuleScoringIsUnchanged() {
        givenBoard(
                entry("first", 1, "Lancia Delta S4", false),
                entry("second", 2, "Audi Sport quattro S1 E2", false),
                entry("dnf", 3, "Audi Sport quattro S1 E2", true));

        List<ChampionshipStanding> standings = clubResultsService.getStandings(configuration);

        assertThat(pointsOf(standings, "first")).isEqualTo(10);
        assertThat(pointsOf(standings, "second")).isEqualTo(8);
        assertThat(pointsOf(standings, "dnf")).isZero();
    }
}
