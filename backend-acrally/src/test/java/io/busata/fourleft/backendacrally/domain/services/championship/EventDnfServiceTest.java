package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantService;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The owner's escape hatch from the one-shot rule. What matters is that it stays owner-only, that
 * it only ever touches an actual DNF, and that the row survives as the audit trail. Pure Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EventDnfServiceTest {

    @Mock EventArmRepository armRepository;
    @Mock EventEntryRepository entryRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock ChampionshipService championshipService;
    @Mock VariantService variantService;

    @InjectMocks EventDnfService service;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID driverId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();

    private EventArm dnfArm() {
        EventArm arm = new EventArm(driverId, eventId, UUID.randomUUID());
        arm.bind(UUID.randomUUID());
        arm.consume(EventArmOutcome.DNF, null);
        return arm;
    }

    @Test
    void revertingStampsTheArmWithoutErasingIt() {
        EventArm arm = dnfArm();
        when(armRepository.findById(arm.getId())).thenReturn(Optional.of(arm));

        service.revert(eventId, arm.getId(), ownerId);

        assertThat(arm.isReverted()).isTrue();
        assertThat(arm.getRevertedBy()).isEqualTo(ownerId);
        // The record of what happened stays — only its effect on the one-shot rule is lifted.
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.DNF);
    }

    @Test
    void revertingIsIdempotent() {
        EventArm arm = dnfArm();
        arm.revert(ownerId);
        java.time.LocalDateTime first = arm.getRevertedAt();
        UUID otherOwner = UUID.randomUUID();
        when(armRepository.findById(arm.getId())).thenReturn(Optional.of(arm));

        service.revert(eventId, arm.getId(), otherOwner);

        assertThat(arm.getRevertedAt()).isEqualTo(first);
        assertThat(arm.getRevertedBy()).isEqualTo(ownerId);
    }

    @Test
    void onlyTheClubOwnerCanRevert() {
        UUID stranger = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(championshipService).requireOwnedEvent(eventId, stranger);

        assertThatThrownBy(() -> service.revert(eventId, UUID.randomUUID(), stranger))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(armRepository, never()).findById(any());
    }

    @Test
    void anArmFromAnotherEventIsNotFound() {
        // The arm id is guessable; the event in the path is what the ownership check covers, so the
        // two have to agree or an owner could revert DNFs on events they don't run.
        EventArm arm = new EventArm(driverId, UUID.randomUUID(), UUID.randomUUID());
        when(armRepository.findById(arm.getId())).thenReturn(Optional.of(arm));

        assertThatThrownBy(() -> service.revert(eventId, arm.getId(), ownerId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void aRecordedRunCannotBeReverted() {
        EventArm arm = new EventArm(driverId, eventId, UUID.randomUUID());
        arm.bind(UUID.randomUUID());
        arm.consume(EventArmOutcome.RECORDED, UUID.randomUUID());
        when(armRepository.findById(arm.getId())).thenReturn(Optional.of(arm));

        assertThatThrownBy(() -> service.revert(eventId, arm.getId(), ownerId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void listingIsOwnerGatedAndEmptyWhenThereAreNoDnfs() {
        when(armRepository.findAllByEventIdAndOutcomeOrderByUpdatedAtDesc(eventId, EventArmOutcome.DNF))
                .thenReturn(List.of());

        assertThat(service.list(eventId, ownerId)).isEmpty();
        verify(championshipService).requireOwnedEvent(eventId, ownerId);
    }
}
