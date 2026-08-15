package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.club.Club;
import io.busata.fourleft.backendacrally.domain.services.club.ClubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The moderation guard: who may act on someone else's event. Club owners are the normal path;
 * a platform admin passes on the role alone, in any club. Everyone else is refused. Pure Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ChampionshipServiceGuardTest {

    @Mock ChampionshipRepository championshipRepository;
    @Mock ChampionshipEventRepository eventRepository;
    @Mock EventVariantRepository eventVariantRepository;
    @Mock EventCarRepository eventCarRepository;
    @Mock ClubRepository clubRepository;
    @Mock io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository variantRepository;
    @Mock io.busata.fourleft.backendacrally.domain.services.car.CarRepository carRepository;
    @Mock io.busata.fourleft.backendacrally.infrastructure.properties.AcrallyProperties properties;

    @InjectMocks ChampionshipService service;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID strangerId = UUID.randomUUID();

    private final Club club = new Club("ACR discord", null, null, ownerId);
    private final Championship championship =
            new Championship(club.getId(), "Weeklong comp", LocalDateTime.now(), ownerId);
    private final ChampionshipEvent event = new ChampionshipEvent(championship.getId(), 0, 0, 7);

    private void stubEvent() {
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
    }

    private void stubClub() {
        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
    }

    @Test
    void theClubOwnerMayModerate() {
        stubEvent();
        stubClub();

        assertThat(service.requireModeratableEvent(event.getId(), ownerId, false)).isEqualTo(event);
    }

    @Test
    void anAdminMayModerateAClubTheyDoNotOwn() {
        stubEvent();

        assertThat(service.requireModeratableEvent(event.getId(), adminId, true)).isEqualTo(event);
        // The role alone settles it — no club is consulted, so an unreachable owner can't block it.
        verifyNoInteractions(clubRepository);
    }

    @Test
    void anOrdinaryMemberMayNot() {
        stubEvent();
        stubClub();

        assertThatThrownBy(() -> service.requireModeratableEvent(event.getId(), strangerId, false))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void anAdminStillGetsA404ForAnEventThatDoesNotExist() {
        UUID missing = UUID.randomUUID();
        when(eventRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireModeratableEvent(missing, adminId, true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
