package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventCar;
import io.busata.fourleft.backendacrally.domain.models.championship.EventEntry;
import io.busata.fourleft.backendacrally.domain.models.session.AgentSession;
import io.busata.fourleft.backendacrally.domain.models.session.StageResult;
import io.busata.fourleft.backendacrally.domain.models.stage.TrackAlias;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.car.CarAliasRepository;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.TrackAliasRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final TrackAliasRepository trackAliasRepository;
    private final CarRepository carRepository;
    private final CarAliasRepository carAliasRepository;
    private final ChampionshipService championshipService;

    /**
     * A freshly opened session binds the driver's waiting arm when the run can be the armed stage —
     * and a bound run is final: it resolves as a recorded result or a DNF, never a retry. Telemetry
     * only carries display names, so the session's track/car resolve through the alias tables: a
     * name that provably points elsewhere (another variant, or a car the event doesn't permit)
     * leaves the arm waiting — free roam and practice cars stay possible while armed — while a
     * match or an unknown/unassigned name binds. Erring toward binding on alias gaps is deliberate:
     * if unlearned names skipped binding, a run on new content could be discarded at the results
     * screen for a free retry.
     *
     * <p>A session opening while the arm is still BOUND to an earlier session means that run was
     * abandoned (a driver runs one stage at a time; the old session's abort only arrives once the
     * agent's save-record wait window lapses) — the shot is spent, so the arm resolves as DNF.
     */
    public void bindToSession(UUID userId, AgentSession session) {
        EventArm arm = armRepository.findFirstByUserIdAndStatusIn(userId,
                        java.util.List.of(EventArmStatus.ARMED, EventArmStatus.BOUND))
                .orElse(null);
        if (arm == null) {
            return;
        }
        if (arm.getStatus() == EventArmStatus.BOUND) {
            if (!session.getId().equals(arm.getSessionId())) {
                arm.consume(EventArmOutcome.DNF, null);
            }
            return;
        }
        if (couldBeArmedStage(arm, session.getTrack()) && couldBePermittedCar(arm.getEventId(), session.getCar())) {
            arm.bind(session.getId());
        }
    }

    /**
     * The bound run ended without a scoreable result (restart, quit, agent death, or a replayed
     * record) — the shot is spent, resolved as DNF. Releasing the arm here instead would make
     * "restart at the results screen" a free retry, because a discarded run never writes a save
     * record for the server to judge.
     */
    public void dnfSession(UUID sessionId) {
        armRepository.findFirstBySessionIdAndStatus(sessionId, EventArmStatus.BOUND)
                .ifPresent(arm -> arm.consume(EventArmOutcome.DNF, null));
    }

    /** Whether the telemetry track could be the armed variant: yes, unless an assigned alias points elsewhere. */
    private boolean couldBeArmedStage(EventArm arm, String track) {
        if (track == null || track.isBlank()) {
            return true;
        }
        return trackAliasRepository.findByRawName(track)
                .map(TrackAlias::getVariantId)
                .filter(java.util.Objects::nonNull)
                .map(variantId -> variantId.equals(arm.getVariantId()))
                .orElse(true);
    }

    /** Whether the telemetry car could be permitted: yes, unless it resolves to a car the event excludes. */
    private boolean couldBePermittedCar(UUID eventId, String car) {
        Set<UUID> permitted = eventCarRepository.findAllByEventId(eventId).stream()
                .map(EventCar::getCarId)
                .collect(Collectors.toSet());
        if (permitted.isEmpty()) {
            return true;
        }
        return resolveCar(car)
                .map(matched -> permitted.contains(matched.getId()))
                .orElse(true);
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
        Optional<Car> matchedCar = resolveCar(result.getCar());
        if (!permitted.isEmpty()
                && (matchedCar.isEmpty() || !permitted.contains(matchedCar.get().getId()))) {
            arm.consume(EventArmOutcome.WRONG_CAR, result.getId());
            return;
        }

        // 3. Still open? The window might have closed while the run was in progress.
        if (!championshipService.isOpenNow(arm.getEventId())) {
            arm.consume(EventArmOutcome.EVENT_CLOSED, result.getId());
            return;
        }

        // 4. Record. One shot per stage means an existing entry should have blocked arming
        //    (EventArmService#arm), so the keep-the-better-time branch is a safety net only.
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

    /**
     * Resolve the raw car string a result carries to a catalogue car: first via an assigned
     * {@code car_alias} (the game's reported name, e.g. "Lancia Delta Integrale Evo"), falling back
     * to an exact catalogue-name match for cars whose game name already equals their catalogue name.
     */
    private Optional<Car> resolveCar(String rawCar) {
        if (rawCar == null || rawCar.isBlank()) {
            return Optional.empty();
        }
        Optional<Car> viaAlias = carAliasRepository.findByRawName(rawCar)
                .map(alias -> alias.getCarId())
                .filter(java.util.Objects::nonNull)
                .flatMap(carRepository::findById);
        if (viaAlias.isPresent()) {
            return viaAlias;
        }
        return carRepository.findFirstByNameIgnoreCase(rawCar);
    }
}
