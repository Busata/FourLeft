package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventCar;
import io.busata.fourleft.backendacrally.domain.models.championship.EventEntry;
import io.busata.fourleft.backendacrally.domain.models.session.StageResult;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ties a run's authoritative result to the arm that captured it (see {@code SessionIngestService}).
 * Binding happens at session-open (only a run that started after Start can bind); this service is the
 * other half — it evaluates a bound arm against the finished result and, if it matches the armed
 * stage in a permitted car while the event is open, writes/updates the driver's best time.
 */
@Service
@RequiredArgsConstructor
public class EventRecordingService {

    private final EventArmRepository armRepository;
    private final EventEntryRepository entryRepository;
    private final EventCarRepository eventCarRepository;
    private final VariantRepository variantRepository;
    private final CarRepository carRepository;
    private final ChampionshipService championshipService;

    /** A freshly opened session binds the driver's waiting arm — that run is the one that counts. */
    public void bindToSession(UUID userId, UUID sessionId) {
        armRepository.findFirstByUserIdAndStatusIn(userId, java.util.List.of(EventArmStatus.ARMED))
                .ifPresent(arm -> arm.bind(sessionId));
    }

    /** An aborted/restarted run releases its arm so the next fresh run can re-bind. */
    public void unbindSession(UUID sessionId) {
        armRepository.findFirstBySessionIdAndStatus(sessionId, EventArmStatus.BOUND)
                .ifPresent(EventArm::unbind);
    }

    /**
     * Evaluate the arm bound to this session (if any) against the finished result and record it when
     * it qualifies. Consumes the arm with the outcome either way. No-op when the session carries no
     * bound arm (an ordinary, un-armed run).
     */
    public void recordIfArmed(UUID sessionId, StageResult result) {
        EventArm arm = armRepository.findFirstBySessionIdAndStatus(sessionId, EventArmStatus.BOUND).orElse(null);
        if (arm == null) {
            return;
        }

        // 1. Right stage? The save-file key must resolve to exactly the armed variant.
        Variant variant = result.getStage() == null ? null
                : variantRepository.findByRawName(result.getStage()).orElse(null);
        if (variant == null || !variant.getId().equals(arm.getVariantId())) {
            arm.consume(EventArmOutcome.WRONG_STAGE, result.getId());
            return;
        }

        // 2. Permitted car? An empty car list means "any car"; otherwise the raw car string must
        //    resolve to a catalogue car the event allows.
        Set<UUID> permitted = eventCarRepository.findAllByEventId(arm.getEventId()).stream()
                .map(EventCar::getCarId)
                .collect(Collectors.toSet());
        Optional<Car> matchedCar = result.getCar() == null ? Optional.empty()
                : carRepository.findFirstByNameIgnoreCase(result.getCar());
        if (!permitted.isEmpty()
                && (matchedCar.isEmpty() || !permitted.contains(matchedCar.get().getId()))) {
            arm.consume(EventArmOutcome.WRONG_CAR, result.getId());
            return;
        }

        // 3. Still open? The window might have closed while the run was in progress.
        if (!championshipService.isOpen(arm.getEventId(), LocalDateTime.now())) {
            arm.consume(EventArmOutcome.EVENT_CLOSED, result.getId());
            return;
        }

        // 4. Record — keep the better time if the driver already has one on this stage.
        UUID carId = matchedCar.map(Car::getId).orElse(null);
        EventEntry existing = entryRepository
                .findByEventIdAndVariantIdAndUserId(arm.getEventId(), variant.getId(), result.getUserId())
                .orElse(null);
        if (existing != null && existing.getTotalMs() <= result.getTotalMs()) {
            arm.consume(EventArmOutcome.SLOWER, result.getId());
            return;
        }
        if (existing == null) {
            entryRepository.save(new EventEntry(
                    arm.getEventId(), variant.getId(), result.getUserId(), carId, result.getCar(),
                    result.getId(), result.getRawMs(), result.getPenaltyMs(), result.getTotalMs(),
                    result.getCreatedAt()));
        } else {
            existing.replaceWith(carId, result.getCar(), result.getId(),
                    result.getRawMs(), result.getPenaltyMs(), result.getTotalMs(), result.getCreatedAt());
        }
        arm.consume(EventArmOutcome.RECORDED, result.getId());
    }
}
