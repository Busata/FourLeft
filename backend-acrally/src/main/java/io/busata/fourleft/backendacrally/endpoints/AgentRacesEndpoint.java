package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.backendacrally.application.races.RacePayloads.ArmRequest;
import io.busata.fourleft.backendacrally.application.races.RacePayloads.ArmState;
import io.busata.fourleft.backendacrally.application.races.RacePayloads.RaceEvent;
import io.busata.fourleft.backendacrally.application.races.RacePayloads.RaceStage;
import io.busata.fourleft.backendacrally.application.races.RacePayloads.RacesView;
import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.car.CarAlias;
import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventCar;
import io.busata.fourleft.backendacrally.domain.models.championship.EventEntry;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.car.CarAliasRepository;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipEventRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipService;
import io.busata.fourleft.backendacrally.domain.services.championship.EventArmService;
import io.busata.fourleft.backendacrally.domain.services.championship.EventCarRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.EventEntryRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.EventVariantRepository;
import io.busata.fourleft.backendacrally.domain.services.club.ClubRepository;
import io.busata.fourleft.backendacrally.domain.services.club.ClubMembershipRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantService;
import io.busata.fourleft.backendacrally.infrastructure.security.AgentPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The agent's Races tab, API-key authenticated (an {@link AgentPrincipal}). Lists the open events of
 * clubs the driver belongs to and drives the Start button: arming a stage, disarming, and reporting
 * the current arm state (which the agent polls to show live warnings + the last run's outcome).
 */
@RestController
@RequestMapping("/acrally-api/agent/races")
@RequiredArgsConstructor
public class AgentRacesEndpoint {

    private final ClubMembershipRepository membershipRepository;
    private final ClubRepository clubRepository;
    private final ChampionshipRepository championshipRepository;
    private final ChampionshipEventRepository eventRepository;
    private final EventVariantRepository eventVariantRepository;
    private final EventCarRepository eventCarRepository;
    private final EventEntryRepository entryRepository;
    private final VariantRepository variantRepository;
    private final CarRepository carRepository;
    private final CarAliasRepository carAliasRepository;
    private final VariantService variantService;
    private final ChampionshipService championshipService;
    private final EventArmService armService;

    @GetMapping
    public RacesView list(@AuthenticationPrincipal AgentPrincipal agent) {
        UUID userId = agent.userId();
        LocalDateTime now = championshipService.now();

        Map<UUID, Variant> variants = byId(variantRepository.findAll(), Variant::getId);
        Map<UUID, VariantService.VariantLabel> labels = variantService.labelsById();
        Map<UUID, String> carNames = carRepository.findAll().stream()
                .collect(Collectors.toMap(Car::getId, Car::getName));
        Map<UUID, List<String>> aliasesByCar = aliasesByCar();

        List<RaceEvent> events = new java.util.ArrayList<>();
        Set<UUID> clubIds = membershipRepository.findClubIdsByUserId(userId);
        if (!clubIds.isEmpty()) {
            Map<UUID, String> clubNames = clubRepository.findAllById(clubIds).stream()
                    .collect(Collectors.toMap(club -> club.getId(), club -> club.getName()));
            List<Championship> champs = championshipRepository
                    .findAllByClubIdInAndStatus(clubIds, ChampionshipStatus.PUBLISHED);
            for (Championship champ : champs) {
                List<ChampionshipEvent> evts =
                        eventRepository.findAllByChampionshipIdOrderByPositionAsc(champ.getId());
                Map<UUID, ChampionshipService.EventWindow> windows = championshipService.windows(champ, evts);
                for (ChampionshipEvent event : evts) {
                    ChampionshipService.EventWindow window = windows.get(event.getId());
                    if (window == null || !window.contains(now)) {
                        continue; // only events currently open for entries
                    }
                    events.add(toRaceEvent(userId, champ, clubNames.get(champ.getClubId()),
                            event, window, variants, labels, carNames, aliasesByCar));
                }
            }
        }
        return new RacesView(events, buildArmState(userId, variants, labels, carNames, aliasesByCar));
    }

    @PostMapping("/arm")
    public ArmState arm(@AuthenticationPrincipal AgentPrincipal agent, @RequestBody ArmRequest request) {
        armService.arm(agent.userId(), request.eventId(), request.variantId());
        return currentArmState(agent.userId());
    }

    @PostMapping("/disarm")
    public ArmState disarm(@AuthenticationPrincipal AgentPrincipal agent) {
        armService.disarm(agent.userId());
        return currentArmState(agent.userId());
    }

    @GetMapping("/arm")
    public ArmState arm(@AuthenticationPrincipal AgentPrincipal agent) {
        return currentArmState(agent.userId());
    }

    // --- Assembly -------------------------------------------------------------------------------

    private RaceEvent toRaceEvent(UUID userId, Championship champ, String clubName, ChampionshipEvent event,
                                  ChampionshipService.EventWindow window, Map<UUID, Variant> variants,
                                  Map<UUID, VariantService.VariantLabel> labels, Map<UUID, String> carNames,
                                  Map<UUID, List<String>> aliasesByCar) {
        List<UUID> permittedCarIds = eventCarRepository.findAllByEventId(event.getId()).stream()
                .map(EventCar::getCarId).toList();
        List<String> permittedCars = permittedCarIds.stream().map(carNames::get).filter(Objects::nonNull)
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();
        List<String> carKeys = carKeysFor(permittedCarIds, carNames, aliasesByCar);
        Map<UUID, Integer> myBest = entryRepository.findByEventIdAndUserId(event.getId(), userId).stream()
                .collect(Collectors.toMap(EventEntry::getVariantId, EventEntry::getTotalMs, Math::min));

        List<RaceStage> stages = eventVariantRepository.findAllByEventIdOrderByPositionAsc(event.getId()).stream()
                .map(EventVariant::getVariantId)
                .map(variantId -> toRaceStage(variantId, variants, labels, permittedCars, carKeys, myBest.get(variantId)))
                .toList();

        return new RaceEvent(event.getId(), champ.getId(), champ.getName(), clubName,
                deriveLabel(stages), window.opensAt().toString(), window.closesAt().toString(), stages);
    }

    private RaceStage toRaceStage(UUID variantId, Map<UUID, Variant> variants,
                                  Map<UUID, VariantService.VariantLabel> labels,
                                  List<String> permittedCars, List<String> carKeys, Integer myBestMs) {
        Variant variant = variants.get(variantId);
        VariantService.VariantLabel label = labels.get(variantId);
        String rawName = variant == null ? null : variant.getRawName();
        String display = label != null ? label.fullLabel() : (rawName != null ? rawName : "(stage)");
        return new RaceStage(variantId, rawName, display,
                label == null ? null : label.stageName(),
                label == null ? null : label.locationName(),
                permittedCars, carKeys, myBestMs);
    }

    private ArmState currentArmState(UUID userId) {
        Map<UUID, Variant> variants = byId(variantRepository.findAll(), Variant::getId);
        Map<UUID, VariantService.VariantLabel> labels = variantService.labelsById();
        Map<UUID, String> carNames = carRepository.findAll().stream()
                .collect(Collectors.toMap(Car::getId, Car::getName));
        return buildArmState(userId, variants, labels, carNames, aliasesByCar());
    }

    private ArmState buildArmState(UUID userId, Map<UUID, Variant> variants,
                                   Map<UUID, VariantService.VariantLabel> labels, Map<UUID, String> carNames,
                                   Map<UUID, List<String>> aliasesByCar) {
        Optional<EventArm> live = armService.liveArm(userId);
        if (live.isPresent()) {
            EventArm arm = live.get();
            Variant variant = variants.get(arm.getVariantId());
            VariantService.VariantLabel label = labels.get(arm.getVariantId());
            List<UUID> permittedCarIds = eventCarRepository.findAllByEventId(arm.getEventId()).stream()
                    .map(EventCar::getCarId).toList();
            List<String> cars = permittedCarIds.stream().map(carNames::get).filter(Objects::nonNull)
                    .sorted(String.CASE_INSENSITIVE_ORDER).toList();
            List<String> carKeys = carKeysFor(permittedCarIds, carNames, aliasesByCar);
            String stageLabel = label != null ? label.fullLabel()
                    : (variant != null ? variant.getRawName() : null);
            return new ArmState(true, arm.getStatus().name(), arm.getEventId(), arm.getVariantId(),
                    stageLabel, variant == null ? null : variant.getRawName(), cars, carKeys, null, null, null);
        }

        Optional<EventArm> latest = armService.latestArm(userId);
        if (latest.isPresent() && latest.get().getStatus() == EventArmStatus.CONSUMED
                && latest.get().getOutcome() != null) {
            EventArm arm = latest.get();
            VariantService.VariantLabel label = labels.get(arm.getVariantId());
            String stageLabel = label != null ? label.fullLabel() : null;
            Integer total = null;
            if (arm.getOutcome() == EventArmOutcome.RECORDED || arm.getOutcome() == EventArmOutcome.SLOWER) {
                total = entryRepository
                        .findByEventIdAndVariantIdAndUserId(arm.getEventId(), arm.getVariantId(), userId)
                        .map(EventEntry::getTotalMs).orElse(null);
            }
            return new ArmState(false, null, null, null, null, null, List.of(), List.of(),
                    arm.getOutcome().name(), stageLabel, total);
        }
        return ArmState.idle();
    }

    /** Raw match strings for a set of cars: each car's catalogue name plus its assigned aliases. */
    private List<String> carKeysFor(List<UUID> carIds, Map<UUID, String> carNames,
                                    Map<UUID, List<String>> aliasesByCar) {
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();
        for (UUID carId : carIds) {
            String name = carNames.get(carId);
            if (name != null) {
                keys.add(name);
            }
            keys.addAll(aliasesByCar.getOrDefault(carId, List.of()));
        }
        return new java.util.ArrayList<>(keys);
    }

    /** Assigned aliases grouped by the car they map to (raw game-reported strings). */
    private Map<UUID, List<String>> aliasesByCar() {
        return carAliasRepository.findAll().stream()
                .filter(alias -> alias.getCarId() != null)
                .collect(Collectors.groupingBy(CarAlias::getCarId,
                        Collectors.mapping(CarAlias::getRawName, Collectors.toList())));
    }

    private String deriveLabel(List<RaceStage> stages) {
        String label = stages.stream().map(RaceStage::locationName).filter(Objects::nonNull)
                .distinct().collect(Collectors.joining(" · "));
        return label.isBlank() ? "Event" : label;
    }

    private <T> Map<UUID, T> byId(List<T> items, java.util.function.Function<T, UUID> id) {
        return items.stream().collect(Collectors.toMap(id, t -> t));
    }
}
