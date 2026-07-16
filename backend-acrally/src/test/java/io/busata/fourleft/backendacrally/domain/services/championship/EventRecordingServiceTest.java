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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The anti-cheat core: how a run's result is (or isn't) recorded against the arm bound to its
 * session. Pure Mockito — no Spring context or DB needed.
 */
@ExtendWith(MockitoExtension.class)
class EventRecordingServiceTest {

    @Mock EventArmRepository armRepository;
    @Mock EventEntryRepository entryRepository;
    @Mock EventCarRepository eventCarRepository;
    @Mock VariantRepository variantRepository;
    @Mock TrackAliasRepository trackAliasRepository;
    @Mock CarRepository carRepository;
    @Mock CarAliasRepository carAliasRepository;
    @Mock ChampionshipService championshipService;

    @InjectMocks EventRecordingService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();

    private EventArm boundArm(UUID variantId) {
        EventArm arm = new EventArm(userId, eventId, variantId);
        arm.bind(sessionId);
        when(armRepository.findFirstBySessionIdAndStatus(sessionId, EventArmStatus.BOUND))
                .thenReturn(Optional.of(arm));
        return arm;
    }

    private StageResult result(String stage, String car, int totalMs) {
        return new StageResult(sessionId, userId, stage, car, "Drv", totalMs, 0, totalMs, 42L, "0.1");
    }

    private AgentSession sessionOn(String track, String car) {
        return new AgentSession(userId, UUID.randomUUID(), "Drv", car, track, track, 0L, "0.4.0");
    }

    private EventArm waitingArm(UUID variantId) {
        EventArm armed = new EventArm(userId, eventId, variantId);
        when(armRepository.findFirstByUserIdAndStatusIn(
                userId, List.of(EventArmStatus.ARMED, EventArmStatus.BOUND)))
                .thenReturn(Optional.of(armed));
        return armed;
    }

    @Test
    void bindsWaitingArmWhenTrackIsUnknown() {
        // An unlearned telemetry name errs toward binding — a gap must never allow a free retry.
        EventArm armed = waitingArm(UUID.randomUUID());
        when(trackAliasRepository.findByRawName("Fresh DLC Stage")).thenReturn(Optional.empty());
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of()); // any car
        AgentSession session = sessionOn("Fresh DLC Stage", "Lancia");

        service.bindToSession(userId, session);

        assertThat(armed.getStatus()).isEqualTo(EventArmStatus.BOUND);
        assertThat(armed.getSessionId()).isEqualTo(session.getId());
    }

    @Test
    void bindsWaitingArmWhenTrackAliasMatchesArmedVariant() {
        UUID variantId = UUID.randomUUID();
        EventArm armed = waitingArm(variantId);
        TrackAlias alias = new TrackAlias("Wales Afon Bidno");
        alias.assign(variantId);
        when(trackAliasRepository.findByRawName("Wales Afon Bidno")).thenReturn(Optional.of(alias));
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of());
        AgentSession session = sessionOn("Wales Afon Bidno", "Mini Cooper S 1275");

        service.bindToSession(userId, session);

        assertThat(armed.getStatus()).isEqualTo(EventArmStatus.BOUND);
        assertThat(armed.getSessionId()).isEqualTo(session.getId());
    }

    @Test
    void freeRoamOnAnotherStageLeavesArmWaiting() {
        EventArm armed = waitingArm(UUID.randomUUID());
        TrackAlias alias = new TrackAlias("Alsace Sommet");
        alias.assign(UUID.randomUUID()); // provably a different variant than the armed one
        when(trackAliasRepository.findByRawName("Alsace Sommet")).thenReturn(Optional.of(alias));

        service.bindToSession(userId, sessionOn("Alsace Sommet", "Lancia"));

        assertThat(armed.getStatus()).isEqualTo(EventArmStatus.ARMED);
        assertThat(armed.getSessionId()).isNull();
    }

    @Test
    void practiceInAnUnpermittedCarLeavesArmWaiting() {
        Car permitted = new Car("Mini Cooper S", 1965, "2", "B");
        Car other = new Car("Lancia Delta", 1992, "A", "A8");
        EventArm armed = waitingArm(UUID.randomUUID());
        when(trackAliasRepository.findByRawName("Wales Afon Bidno")).thenReturn(Optional.empty());
        when(eventCarRepository.findAllByEventId(eventId))
                .thenReturn(List.of(new EventCar(eventId, permitted.getId())));
        when(carRepository.findFirstByNameIgnoreCase("Lancia Delta")).thenReturn(Optional.of(other));

        service.bindToSession(userId, sessionOn("Wales Afon Bidno", "Lancia Delta"));

        assertThat(armed.getStatus()).isEqualTo(EventArmStatus.ARMED);
        assertThat(armed.getSessionId()).isNull();
    }

    @Test
    void bindsWhenCarIsUnknownOnACarRestrictedEvent() {
        // An unresolvable car string errs toward binding, like an unlearned track name.
        EventArm armed = waitingArm(UUID.randomUUID());
        when(trackAliasRepository.findByRawName("Wales Afon Bidno")).thenReturn(Optional.empty());
        when(eventCarRepository.findAllByEventId(eventId))
                .thenReturn(List.of(new EventCar(eventId, UUID.randomUUID())));
        when(carRepository.findFirstByNameIgnoreCase("Mystery_Car")).thenReturn(Optional.empty());
        AgentSession session = sessionOn("Wales Afon Bidno", "Mystery_Car");

        service.bindToSession(userId, session);

        assertThat(armed.getStatus()).isEqualTo(EventArmStatus.BOUND);
        assertThat(armed.getSessionId()).isEqualTo(session.getId());
    }

    @Test
    void abandonedBoundRunResolvesAsDnfWhenTheNextSessionOpens() {
        // Restart-at-results-screen: the old session's abort lags behind the next session's open.
        // A new run while one is bound proves the bound run was abandoned — the shot is spent.
        EventArm arm = new EventArm(userId, eventId, UUID.randomUUID());
        UUID abandonedSessionId = UUID.randomUUID();
        arm.bind(abandonedSessionId);
        when(armRepository.findFirstByUserIdAndStatusIn(
                userId, List.of(EventArmStatus.ARMED, EventArmStatus.BOUND)))
                .thenReturn(Optional.of(arm));

        service.bindToSession(userId, sessionOn("Wales Afon Bidno", "Mini Cooper S 1275"));

        assertThat(arm.getStatus()).isEqualTo(EventArmStatus.CONSUMED);
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.DNF);
        assertThat(arm.getSessionId()).isEqualTo(abandonedSessionId); // the new run never binds
    }

    @Test
    void dnfSessionConsumesBoundArm() {
        EventArm arm = new EventArm(userId, eventId, UUID.randomUUID());
        arm.bind(sessionId);
        when(armRepository.findFirstBySessionIdAndStatus(sessionId, EventArmStatus.BOUND))
                .thenReturn(Optional.of(arm));

        service.dnfSession(sessionId);

        assertThat(arm.getStatus()).isEqualTo(EventArmStatus.CONSUMED);
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.DNF);
        assertThat(arm.getResultId()).isNull();
    }

    @Test
    void unarmedRunRecordsNothing() {
        when(armRepository.findFirstBySessionIdAndStatus(sessionId, EventArmStatus.BOUND))
                .thenReturn(Optional.empty());

        service.recordIfArmed(sessionId, result("Stage", "Car", 90_000));

        verify(entryRepository, never()).save(any());
    }

    @Test
    void recordsFirstTimeOnMatch() {
        Variant variant = new Variant("StageKey");
        EventArm arm = boundArm(variant.getId());
        when(variantRepository.findByRawName("StageKey")).thenReturn(Optional.of(variant));
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of()); // any car
        when(championshipService.isOpenNow(eventId)).thenReturn(true);
        when(entryRepository.findByEventIdAndVariantIdAndUserId(eventId, variant.getId(), userId))
                .thenReturn(Optional.empty());

        service.recordIfArmed(sessionId, result("StageKey", "Lancia", 100_000));

        verify(entryRepository).save(any(EventEntry.class));
        assertThat(arm.getStatus()).isEqualTo(EventArmStatus.CONSUMED);
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.RECORDED);
    }

    @Test
    void wrongStageIsRejected() {
        EventArm arm = boundArm(UUID.randomUUID()); // armed variant differs from what was driven
        when(variantRepository.findByRawName("OtherKey")).thenReturn(Optional.of(new Variant("OtherKey")));

        service.recordIfArmed(sessionId, result("OtherKey", "Lancia", 100_000));

        verify(entryRepository, never()).save(any());
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.WRONG_STAGE);
    }

    @Test
    void wrongCarIsRejected() {
        Variant variant = new Variant("StageKey");
        Car permitted = new Car("Lancia Delta", 1992, "A", "A8");
        EventArm arm = boundArm(variant.getId());
        when(variantRepository.findByRawName("StageKey")).thenReturn(Optional.of(variant));
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of(new EventCar(eventId, permitted.getId())));
        when(carRepository.findFirstByNameIgnoreCase("Ford Escort")).thenReturn(Optional.empty());

        service.recordIfArmed(sessionId, result("StageKey", "Ford Escort", 100_000));

        verify(entryRepository, never()).save(any());
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.WRONG_CAR);
    }

    @Test
    void closedEventIsRejected() {
        Variant variant = new Variant("StageKey");
        EventArm arm = boundArm(variant.getId());
        when(variantRepository.findByRawName("StageKey")).thenReturn(Optional.of(variant));
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of());
        when(championshipService.isOpenNow(eventId)).thenReturn(false);

        service.recordIfArmed(sessionId, result("StageKey", "Lancia", 100_000));

        verify(entryRepository, never()).save(any());
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.EVENT_CLOSED);
    }

    @Test
    void slowerRunKeepsExistingBest() {
        Variant variant = new Variant("StageKey");
        EventArm arm = boundArm(variant.getId());
        EventEntry existing = new EventEntry(eventId, variant.getId(), userId, null, "Lancia",
                UUID.randomUUID(), 90_000, 0, 90_000, LocalDateTime.now());
        when(variantRepository.findByRawName("StageKey")).thenReturn(Optional.of(variant));
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of());
        when(championshipService.isOpenNow(eventId)).thenReturn(true);
        when(entryRepository.findByEventIdAndVariantIdAndUserId(eventId, variant.getId(), userId))
                .thenReturn(Optional.of(existing));

        service.recordIfArmed(sessionId, result("StageKey", "Lancia", 100_000));

        verify(entryRepository, never()).save(any());
        assertThat(existing.getTotalMs()).isEqualTo(90_000); // untouched
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.SLOWER);
    }

    @Test
    void fasterRunReplacesExistingBest() {
        Variant variant = new Variant("StageKey");
        EventArm arm = boundArm(variant.getId());
        EventEntry existing = new EventEntry(eventId, variant.getId(), userId, null, "Lancia",
                UUID.randomUUID(), 200_000, 0, 200_000, LocalDateTime.now());
        when(variantRepository.findByRawName("StageKey")).thenReturn(Optional.of(variant));
        when(eventCarRepository.findAllByEventId(eventId)).thenReturn(List.of());
        when(championshipService.isOpenNow(eventId)).thenReturn(true);
        when(entryRepository.findByEventIdAndVariantIdAndUserId(eventId, variant.getId(), userId))
                .thenReturn(Optional.of(existing));
        // Belt-and-braces: this test never saves a new entry, only replaces in place.
        lenient().when(entryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.recordIfArmed(sessionId, result("StageKey", "Lancia", 100_000));

        assertThat(existing.getTotalMs()).isEqualTo(100_000); // replaced with the faster run
        assertThat(arm.getOutcome()).isEqualTo(EventArmOutcome.RECORDED);
    }
}
