package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.services.club.ClubMembershipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The disarm side of the anti-cheat: a BOUND arm (run in progress) must be un-cancellable,
 * or bailing out of a bad run before the finish keeps the old time. Pure Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EventArmServiceTest {

    @Mock EventArmRepository armRepository;
    @Mock ChampionshipEventRepository eventRepository;
    @Mock EventVariantRepository eventVariantRepository;
    @Mock ChampionshipRepository championshipRepository;
    @Mock ClubMembershipRepository membershipRepository;
    @Mock ChampionshipService championshipService;

    @InjectMocks EventArmService service;

    private final UUID userId = UUID.randomUUID();

    private EventArm liveArm(boolean bound) {
        EventArm arm = new EventArm(userId, UUID.randomUUID(), UUID.randomUUID());
        if (bound) {
            arm.bind(UUID.randomUUID());
        }
        return arm;
    }

    @Test
    void disarmCancelsAnArmedArm() {
        EventArm arm = liveArm(false);
        when(armRepository.findFirstByUserIdAndStatusIn(eq(userId), any(List.class)))
                .thenReturn(Optional.of(arm));

        service.disarm(userId);

        verify(armRepository).saveAndFlush(arm);
    }

    @Test
    void disarmWithoutALiveArmIsIdempotent() {
        when(armRepository.findFirstByUserIdAndStatusIn(eq(userId), any(List.class)))
                .thenReturn(Optional.empty());

        assertThatCode(() -> service.disarm(userId)).doesNotThrowAnyException();
    }

    @Test
    void disarmIsRejectedWhileARunIsBound() {
        EventArm arm = liveArm(true);
        when(armRepository.findFirstByUserIdAndStatusIn(eq(userId), any(List.class)))
                .thenReturn(Optional.of(arm));

        assertThatThrownBy(() -> service.disarm(userId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(armRepository, never()).saveAndFlush(any());
    }

    @Test
    void idleArmedArmsExpireAsDnf() {
        EventArm arm = liveArm(false);
        when(armRepository.findArmedAndIdleSince(any())).thenReturn(List.of(arm));

        int expired = service.expireIdleArms(java.time.LocalDateTime.now().minusHours(6));

        assertThat(expired).isEqualTo(1);
        assertThat(arm.getStatus()).isEqualTo(EventArmStatus.EXPIRED);
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.DNF);
    }

    @Test
    void armingAnotherStageIsRejectedWhileARunIsBound() {
        // Switching stages mid-run would be a disarm by another name — the guard has to fire on
        // the arm path too. Exercise the full path: event exists, member of club, open, variant ok.
        EventArm arm = liveArm(true);
        Championship championship =
                new Championship(UUID.randomUUID(), "Champ", java.time.LocalDateTime.now(), userId);
        ChampionshipEvent event = new ChampionshipEvent(championship.getId(), 0, 0, 7);
        UUID variantId = UUID.randomUUID();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(championshipRepository.findById(championship.getId())).thenReturn(Optional.of(championship));
        when(membershipRepository.existsByClubIdAndUserId(championship.getClubId(), userId)).thenReturn(true);
        when(championshipService.isOpenNow(event.getId())).thenReturn(true);
        when(eventVariantRepository.findAllByEventIdOrderByPositionAsc(event.getId()))
                .thenReturn(List.of(new EventVariant(event.getId(), variantId, 0)));
        when(armRepository.findFirstByUserIdAndStatusIn(eq(userId), any(List.class)))
                .thenReturn(Optional.of(arm));

        assertThatThrownBy(() -> service.arm(userId, event.getId(), variantId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(armRepository, never()).save(any(EventArm.class));
    }
}
