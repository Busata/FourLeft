package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventEntry;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Standings derivation: per-stage boards sort fastest-first, and the overall ranks completing more
 * stages above a faster partial. Pure Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EventLeaderboardServiceTest {

    @Mock EventEntryRepository entryRepository;
    @Mock EventVariantRepository eventVariantRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock io.busata.fourleft.backendacrally.domain.services.car.CarRepository carRepository;

    @InjectMocks EventLeaderboardService service;

    private final UUID eventId = UUID.randomUUID();
    private final UUID stage1 = UUID.randomUUID();
    private final UUID stage2 = UUID.randomUUID();

    private EventEntry entry(UUID variantId, UUID userId, int totalMs) {
        return new EventEntry(eventId, variantId, userId, null, "Car",
                UUID.randomUUID(), totalMs, 0, totalMs, LocalDateTime.now());
    }

    @Test
    void boardsSortFastestFirstAndOverallFavoursCompletion() {
        UUID full = UUID.randomUUID();    // drives both stages (slower per stage)
        UUID partial = UUID.randomUUID(); // drives only stage 1 (faster there)

        when(entryRepository.findByEventId(eventId)).thenReturn(List.of(
                entry(stage1, full, 130_000),
                entry(stage1, partial, 100_000),
                entry(stage2, full, 140_000)));
        when(eventVariantRepository.findAllByEventIdOrderByPositionAsc(eventId)).thenReturn(List.of(
                new EventVariant(eventId, stage1, 0),
                new EventVariant(eventId, stage2, 1)));
        AppUser fullUser = user(full, "Full");
        AppUser partialUser = user(partial, "Partial");
        when(appUserRepository.findAllById(org.mockito.ArgumentMatchers.anyIterable()))
                .thenReturn(List.of(fullUser, partialUser));

        EventLeaderboardService.EventStandings standings = service.standings(eventId);

        // Stage 1 board: the faster partial-driver leads.
        List<EventLeaderboardService.BoardRow> board1 = standings.stages().get(0).rows();
        assertThat(board1).extracting(EventLeaderboardService.BoardRow::userId)
                .containsExactly(partial, full);

        // Overall: completing both stages (270s total) outranks a faster single stage (100s).
        assertThat(standings.overall()).extracting(EventLeaderboardService.Standing::userId)
                .containsExactly(full, partial);
        assertThat(standings.overall().get(0).stagesCompleted()).isEqualTo(2);
        assertThat(standings.overall().get(1).stagesCompleted()).isEqualTo(1);
    }

    private AppUser user(UUID id, String name) {
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        when(user.getId()).thenReturn(id);
        when(user.getDisplayName()).thenReturn(name);
        return user;
    }
}
