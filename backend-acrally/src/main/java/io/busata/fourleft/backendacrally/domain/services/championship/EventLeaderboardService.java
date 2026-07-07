package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.championship.EventEntry;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Reads the recorded {@link EventEntry} rows into leaderboards: one best-time board per stage
 * (ordered by the event's stage running order) plus the event's overall standings — each driver's
 * summed best across the stages they've completed. Ordering and summing live here; the endpoint only
 * labels the variants. Driver names are batch-resolved to avoid an N+1.
 */
@Service
@RequiredArgsConstructor
public class EventLeaderboardService {

    private final EventEntryRepository entryRepository;
    private final EventVariantRepository eventVariantRepository;
    private final AppUserRepository appUserRepository;
    private final CarRepository carRepository;

    /** A single driver's time on one stage. */
    public record BoardRow(UUID userId, String driver, String carName,
                           int rawMs, int penaltyMs, int totalMs, LocalDateTime recordedAt) {
    }

    /** One stage's board (fastest first). */
    public record StageBoard(UUID variantId, List<BoardRow> rows) {
    }

    /** A driver's overall standing: summed best across completed stages. */
    public record Standing(UUID userId, String driver, long totalMs, int stagesCompleted) {
    }

    /** The full picture for an event: per-stage boards + the derived overall standings. */
    public record EventStandings(List<StageBoard> stages, List<Standing> overall) {
    }

    public EventStandings standings(UUID eventId) {
        List<EventEntry> entries = entryRepository.findByEventId(eventId);
        Map<UUID, String> driverNames = resolveDrivers(entries);
        Map<UUID, String> carNames = resolveCars(entries);

        // Stage boards, ordered by the event's running order; each board sorted fastest-first.
        List<UUID> orderedVariantIds = eventVariantRepository.findAllByEventIdOrderByPositionAsc(eventId).stream()
                .map(EventVariant::getVariantId)
                .toList();
        Map<UUID, List<EventEntry>> byVariant = entries.stream()
                .collect(Collectors.groupingBy(EventEntry::getVariantId));
        List<StageBoard> stages = orderedVariantIds.stream()
                .map(variantId -> new StageBoard(variantId, byVariant.getOrDefault(variantId, List.of()).stream()
                        .sorted(Comparator.comparingInt(EventEntry::getTotalMs))
                        .map(e -> row(e, driverNames, carNames))
                        .toList()))
                .toList();

        // Overall: sum each driver's best across the stages they've completed. More stages beats a
        // faster partial sum, so rank by (stagesCompleted desc, summed total asc).
        Map<UUID, List<EventEntry>> byUser = entries.stream()
                .collect(Collectors.groupingBy(EventEntry::getUserId));
        List<Standing> overall = byUser.entrySet().stream()
                .map(e -> new Standing(
                        e.getKey(),
                        driverNames.getOrDefault(e.getKey(), "—"),
                        e.getValue().stream().mapToLong(EventEntry::getTotalMs).sum(),
                        e.getValue().size()))
                .sorted(Comparator.comparingInt(Standing::stagesCompleted).reversed()
                        .thenComparingLong(Standing::totalMs))
                .toList();

        return new EventStandings(stages, overall);
    }

    private BoardRow row(EventEntry e, Map<UUID, String> driverNames, Map<UUID, String> carNames) {
        // Prefer the resolved catalogue car name; fall back to the raw game string when unmapped.
        String carName = e.getCarId() == null ? e.getCarName()
                : carNames.getOrDefault(e.getCarId(), e.getCarName());
        return new BoardRow(e.getUserId(), driverNames.getOrDefault(e.getUserId(), "—"),
                carName, e.getRawMs(), e.getPenaltyMs(), e.getTotalMs(), e.getRecordedAt());
    }

    private Map<UUID, String> resolveDrivers(List<EventEntry> entries) {
        List<UUID> userIds = entries.stream().map(EventEntry::getUserId).distinct().toList();
        return appUserRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getDisplayName));
    }

    private Map<UUID, String> resolveCars(List<EventEntry> entries) {
        List<UUID> carIds = entries.stream()
                .map(EventEntry::getCarId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return carRepository.findAllById(carIds).stream()
                .collect(Collectors.toMap(Car::getId, Car::getName));
    }
}
