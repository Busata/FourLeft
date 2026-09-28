package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.api.easportswrc.models.RestrictionTargetsTo;
import io.busata.fourleft.api.easportswrc.models.RestrictionTargetsTo.RestrictionTargetChampionshipTo;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.configuration.ChannelConfigurationRequest;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Restriction targets of a multi-club channel: every club's open championships, in channel order and tagged
 * with the club, since a rule targets one club's own championship/event ids.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChannelConfigurationRequestServiceRestrictionTargetsTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now();
    private static final UUID REQUEST_ID = UUID.randomUUID();

    @Mock ChannelConfigurationRequestRepository requestRepository;
    @Mock DiscordClubConfigurationService clubConfigurationService;
    @Mock ClubService clubService;

    @InjectMocks ChannelConfigurationRequestService service;

    DiscordClubConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new DiscordClubConfiguration(0L, 42L, "wrc", true);
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(new ChannelConfigurationRequest(0L, 42L, "user")));
        when(clubConfigurationService.findByChannelId(42L)).thenReturn(Optional.of(configuration));
        when(clubService.exists(anyString())).thenReturn(false);
    }

    @Test
    void everyClubsOpenChampionshipIsATargetTaggedWithItsClub() {
        configuration.addClub("wrc2", "Second tier");
        givenClub("wrc", "WRC");
        givenClub("wrc2", "WRC2");

        List<RestrictionTargetChampionshipTo> championships = service.getRestrictionTargets(REQUEST_ID)
                .map(RestrictionTargetsTo::championships).orElseThrow();

        assertThat(championships).extracting(RestrictionTargetChampionshipTo::clubId).containsExactly("wrc", "wrc2");
        // Unlabelled clubs fall back to their car class.
        assertThat(championships).extracting(RestrictionTargetChampionshipTo::clubTag).containsExactly("WRC", "Second tier");
        assertThat(championships).extracting(RestrictionTargetChampionshipTo::id).containsExactly("champ-wrc", "champ-wrc2");
        assertThat(championships.get(1).events()).singleElement().extracting(RestrictionTargetsTo.RestrictionTargetEventTo::id).isEqualTo("event-wrc2");
    }

    @Test
    void aClubThatHasNotBeenImportedYetIsSkipped() {
        configuration.addClub("wrc2", null);
        givenClub("wrc", "WRC");

        List<RestrictionTargetChampionshipTo> championships = service.getRestrictionTargets(REQUEST_ID)
                .map(RestrictionTargetsTo::championships).orElseThrow();

        assertThat(championships).extracting(RestrictionTargetChampionshipTo::clubId).containsExactly("wrc");
    }

    private void givenClub(String clubId, String vehicleClass) {
        Event event = new Event("event-" + clubId, "board-" + clubId, NOW.minusDays(1), NOW.plusDays(1), 1L,
                new EventSettings(1L, vehicleClass, 1L, "Summer", 1L, "Finland", ""));
        Championship championship = new Championship("champ-" + clubId, new ChampionshipSettings(), NOW.minusDays(1), NOW.plusDays(30));
        championship.updateEvents(new ArrayList<>(List.of(event)));
        Club club = new Club(clubId, clubId, "", 10L, NOW.minusYears(1));
        club.updateChampionship(championship);
        when(clubService.exists(clubId)).thenReturn(true);
        when(clubService.findById(clubId)).thenReturn(club);
    }
}
