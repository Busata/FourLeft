package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.models.StageSettings;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.common.ChannelClubMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChannelClubCompatibilityServiceTest {

    // Truncated so "the same schedule" built twice is identical to the second.
    private static final ZonedDateTime NOW = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS);

    @Mock
    ClubService clubService;

    @InjectMocks
    ChannelClubCompatibilityService service;

    DiscordClubConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new DiscordClubConfiguration(0L, 0L, "wrc", true);
        configuration.addClub("wrc2", "WRC2");
        configuration.setMode(ChannelClubMode.MIXED);
        when(clubService.exists(anyString())).thenAnswer(invocation -> false);
    }

    @Test
    void sameChampionshipWithADifferentClassIsMixed() {
        givenClub("wrc", championship(event(1L, "WRC", 0), event(2L, "WRC", 7)));
        givenClub("wrc2", championship(event(1L, "WRC2", 0), event(2L, "WRC2", 7)));

        ChannelClubCompatibility result = service.evaluate(configuration);

        assertThat(result.problems()).isEmpty();
        assertThat(result.compatible()).isTrue();
        assertThat(result.clubs()).extracting(ChannelClubCompatibility.ClubChampionship::vehicleClass).containsExactly("WRC", "WRC2");
        assertThat(service.effectiveMode(configuration)).isEqualTo(ChannelClubMode.MIXED);
    }

    @Test
    void aDifferentLocationFallsBackToSingle() {
        givenClub("wrc", championship(event(1L, "WRC", 0)));
        givenClub("wrc2", championship(event(9L, "WRC2", 0)));

        ChannelClubCompatibility result = service.evaluate(configuration);

        assertThat(result.compatible()).isFalse();
        assertThat(result.problems()).singleElement().asString().contains("location");
        assertThat(service.effectiveMode(configuration)).isEqualTo(ChannelClubMode.SINGLE);
    }

    @Test
    void aDifferentEventCountIsReported() {
        givenClub("wrc", championship(event(1L, "WRC", 0), event(2L, "WRC", 7)));
        givenClub("wrc2", championship(event(1L, "WRC2", 0)));

        assertThat(service.evaluate(configuration).problems()).singleElement().asString().contains("1 events");
    }

    @Test
    void differentStagesAreReported() {
        givenClub("wrc", championship(event(1L, "WRC", 0, 100L)));
        givenClub("wrc2", championship(event(1L, "WRC2", 0, 200L)));

        assertThat(service.evaluate(configuration).problems()).singleElement().asString().contains("stages");
    }

    @Test
    void aScheduleBeyondTheToleranceIsReported() {
        givenClub("wrc", championship(event(1L, "WRC", 0)));
        Event shifted = event(1L, "WRC2", 0);
        shifted = new Event("late", "late", shifted.getAbsoluteOpenDate().plusHours(2), shifted.getAbsoluteCloseDate().plusHours(2), 2L, shifted.getEventSettings());
        shifted.updateStages(List.of(stage(100L)));
        givenClub("wrc2", championship(shifted));

        assertThat(service.evaluate(configuration).problems()).singleElement().asString().contains("different time");
    }

    @Test
    void anUnsyncedSecondClubIsReported() {
        givenClub("wrc", championship(event(1L, "WRC", 0)));

        assertThat(service.evaluate(configuration).problems()).singleElement().asString().contains("hasn't been synced");
    }

    @Test
    void singleModeIgnoresCompatibility() {
        configuration.setMode(ChannelClubMode.SINGLE);
        givenClub("wrc", championship(event(1L, "WRC", 0)));
        givenClub("wrc2", championship(event(1L, "WRC2", 0)));

        assertThat(service.effectiveMode(configuration)).isEqualTo(ChannelClubMode.SINGLE);
    }

    private void givenClub(String clubId, Championship championship) {
        Club club = new Club(clubId, clubId, "", 10L, NOW.minusYears(1));
        club.updateChampionship(championship);
        when(clubService.exists(clubId)).thenReturn(true);
        when(clubService.findById(clubId)).thenReturn(club);
    }

    private static Championship championship(Event... events) {
        Championship championship = new Championship("champ-" + events[0].getEventSettings().getVehicleClass(),
                new ChampionshipSettings(), NOW.minusDays(1), NOW.plusDays(30));
        championship.updateEvents(new ArrayList<>(List.of(events)));
        return championship;
    }

    private static Event event(long locationId, String vehicleClass, int startDay, long... routes) {
        EventSettings settings = new EventSettings(vehicleClass.hashCode() * 1L, vehicleClass, 1L, "Summer", locationId, "Location " + locationId, "");
        Event event = new Event(vehicleClass + "-" + startDay, vehicleClass + "-" + startDay,
                NOW.plusDays(startDay).minusDays(1), NOW.plusDays(startDay + 6), 2L, settings);
        long[] routeIds = routes.length == 0 ? new long[]{100L} : routes;
        List<Stage> stages = new ArrayList<>();
        for (long routeId : routeIds) {
            stages.add(stage(routeId));
        }
        event.updateStages(stages);
        return event;
    }

    private static Stage stage(long routeId) {
        return new Stage("stage-" + routeId + "-" + Math.random(), "board", new StageSettings(routeId, "Route", 1L, "Dry", 1L, "Day", 1L, "Service"));
    }
}
