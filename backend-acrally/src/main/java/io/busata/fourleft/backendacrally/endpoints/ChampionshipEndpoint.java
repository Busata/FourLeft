package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.CarTo;
import io.busata.fourleft.api.acrally.models.ChampionshipDetailTo;
import io.busata.fourleft.api.acrally.models.ChampionshipEventTo;
import io.busata.fourleft.api.acrally.models.ChampionshipTo;
import io.busata.fourleft.api.acrally.models.CreateChampionshipRequestTo;
import io.busata.fourleft.api.acrally.models.CreateEventRequestTo;
import io.busata.fourleft.api.acrally.models.EventVariantTo;
import io.busata.fourleft.api.acrally.models.ReorderEventsRequestTo;
import io.busata.fourleft.api.acrally.models.SetEventCarsRequestTo;
import io.busata.fourleft.api.acrally.models.SetEventVariantsRequestTo;
import io.busata.fourleft.api.acrally.models.UpdateChampionshipRequestTo;
import io.busata.fourleft.api.acrally.models.UpsertEventRequestTo;
import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventCar;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.models.club.Club;
import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipEventRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.ChampionshipService;
import io.busata.fourleft.backendacrally.domain.services.championship.EventCarRepository;
import io.busata.fourleft.backendacrally.domain.services.championship.EventVariantRepository;
import io.busata.fourleft.backendacrally.domain.services.club.ClubRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.LocationRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.StageRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Championship scheduling within a club. Mutations are owner-gated in {@link ChampionshipService};
 * this endpoint additionally hides draft championships from non-owners. Every mutation returns the
 * fresh detail aggregate so the client can rebind without a second round-trip.
 */
@RestController
@RequestMapping("/acrally-api")
@RequiredArgsConstructor
public class ChampionshipEndpoint {

    private final ChampionshipService championshipService;
    private final ChampionshipEventRepository eventRepository;
    private final EventVariantRepository eventVariantRepository;
    private final EventCarRepository eventCarRepository;
    private final ClubRepository clubRepository;
    private final VariantRepository variantRepository;
    private final StageRepository stageRepository;
    private final LocationRepository locationRepository;
    private final CarRepository carRepository;

    @GetMapping("/clubs/{clubId}/championships")
    public List<ChampionshipTo> list(@PathVariable UUID clubId, @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        boolean owner = clubRepository.findById(clubId)
                .map(club -> club.getCreatedBy().equals(userId))
                .orElse(false);
        return championshipService.listForClub(clubId).stream()
                .filter(c -> owner || c.getStatus() == ChampionshipStatus.PUBLISHED)
                .map(c -> new ChampionshipTo(
                        c.getId(),
                        c.getClubId(),
                        c.getName(),
                        c.getStartsAt(),
                        c.getStatus().name(),
                        (int) eventRepository.countByChampionshipId(c.getId()),
                        owner,
                        c.getCreatedAt()))
                .toList();
    }

    @PostMapping("/clubs/{clubId}/championships")
    @ResponseStatus(HttpStatus.CREATED)
    public ChampionshipDetailTo create(@PathVariable UUID clubId,
                                       @RequestBody CreateChampionshipRequestTo request,
                                       @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        Championship championship = championshipService.create(clubId, userId, request.name(), request.startsAt());
        return buildDetail(championship, userId);
    }

    @GetMapping("/championships/{id}")
    public ChampionshipDetailTo get(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        Championship championship = championshipService.get(id);
        boolean owner = championshipService.isOwner(championship, userId);
        if (championship.getStatus() == ChampionshipStatus.DRAFT && !owner) {
            // Don't reveal that a draft exists to non-owners.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such championship.");
        }
        return buildDetail(championship, userId);
    }

    @PutMapping("/championships/{id}")
    public ChampionshipDetailTo update(@PathVariable UUID id,
                                       @RequestBody UpdateChampionshipRequestTo request,
                                       @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        Championship championship = championshipService.update(
                id, userId, request.name(), request.startsAt(), request.status());
        return buildDetail(championship, userId);
    }

    @DeleteMapping("/championships/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        championshipService.delete(id, userId);
    }

    @PostMapping("/championships/{id}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public ChampionshipDetailTo addEvent(@PathVariable UUID id,
                                         @RequestBody CreateEventRequestTo request,
                                         @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        championshipService.addEvent(id, userId, request.gapDays(), request.durationDays(),
                request.variantIds(), request.carIds());
        return buildDetail(championshipService.get(id), userId);
    }

    @PutMapping("/championships/{id}/events/order")
    public ChampionshipDetailTo reorderEvents(@PathVariable UUID id,
                                              @RequestBody ReorderEventsRequestTo request,
                                              @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        championshipService.reorderEvents(id, userId, request.eventIds());
        return buildDetail(championshipService.get(id), userId);
    }

    @PutMapping("/events/{eventId}")
    public ChampionshipDetailTo updateEvent(@PathVariable UUID eventId,
                                            @RequestBody UpsertEventRequestTo request,
                                            @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        ChampionshipEvent event = championshipService.updateEvent(
                eventId, userId, request.gapDays(), request.durationDays());
        return buildDetail(championshipService.get(event.getChampionshipId()), userId);
    }

    @DeleteMapping("/events/{eventId}")
    public ChampionshipDetailTo deleteEvent(@PathVariable UUID eventId,
                                            @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        UUID championshipId = eventRepository.findById(eventId)
                .map(ChampionshipEvent::getChampionshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event."));
        championshipService.deleteEvent(eventId, userId);
        return buildDetail(championshipService.get(championshipId), userId);
    }

    @PutMapping("/events/{eventId}/variants")
    public ChampionshipDetailTo setEventVariants(@PathVariable UUID eventId,
                                                 @RequestBody SetEventVariantsRequestTo request,
                                                 @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        championshipService.setEventVariants(eventId, userId, request.variantIds());
        return buildDetail(championshipService.get(championshipIdOf(eventId)), userId);
    }

    @PutMapping("/events/{eventId}/cars")
    public ChampionshipDetailTo setEventCars(@PathVariable UUID eventId,
                                             @RequestBody SetEventCarsRequestTo request,
                                             @AuthenticationPrincipal AppUserDetails principal) {
        UUID userId = requireLogin(principal);
        championshipService.setEventCars(eventId, userId, request.carIds());
        return buildDetail(championshipService.get(championshipIdOf(eventId)), userId);
    }

    // --- Detail assembly ------------------------------------------------------------------------

    /** Builds the full championship aggregate: ordered events with derived dates, variants and cars. */
    private ChampionshipDetailTo buildDetail(Championship championship, UUID viewerId) {
        List<ChampionshipEvent> events =
                eventRepository.findAllByChampionshipIdOrderByPositionAsc(championship.getId());
        List<UUID> eventIds = events.stream().map(ChampionshipEvent::getId).toList();

        Map<UUID, List<EventVariant>> variantsByEvent = eventIds.isEmpty() ? Map.of()
                : eventVariantRepository.findAllByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(EventVariant::getEventId));
        Map<UUID, List<EventCar>> carsByEvent = eventIds.isEmpty() ? Map.of()
                : eventCarRepository.findAllByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(EventCar::getEventId));

        // Lookups for labelling variants (variant -> stage -> location) and rendering cars.
        Map<UUID, Variant> variants = variantRepository.findAll().stream()
                .collect(Collectors.toMap(Variant::getId, v -> v));
        Map<UUID, Stage> stages = stageRepository.findAll().stream()
                .collect(Collectors.toMap(Stage::getId, s -> s));
        Map<UUID, String> locationNames = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));
        Map<UUID, Car> cars = carRepository.findAll().stream()
                .collect(Collectors.toMap(Car::getId, c -> c));

        LocalDateTime runningStart = championship.getStartsAt();
        List<ChampionshipEventTo> eventTos = new java.util.ArrayList<>();
        for (ChampionshipEvent event : events) {
            LocalDateTime open = runningStart.plusDays(event.getGapDays());
            LocalDateTime close = open.plusDays(event.getDurationDays());
            runningStart = close;

            List<EventVariantTo> variantTos = variantsByEvent.getOrDefault(event.getId(), List.of()).stream()
                    .sorted(java.util.Comparator.comparingInt(EventVariant::getPosition))
                    .map(ev -> toEventVariantTo(ev, variants, stages, locationNames))
                    .toList();
            List<CarTo> carTos = carsByEvent.getOrDefault(event.getId(), List.of()).stream()
                    .map(ec -> cars.get(ec.getCarId()))
                    .filter(java.util.Objects::nonNull)
                    .sorted(java.util.Comparator.comparing(Car::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toCarTo)
                    .toList();

            // Label the event by the distinct locations of its stages (order preserved).
            String label = variantTos.stream()
                    .map(EventVariantTo::locationName)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(" · "));
            if (label.isBlank()) {
                label = "New event";
            }

            eventTos.add(new ChampionshipEventTo(
                    event.getId(), label, event.getPosition(),
                    event.getGapDays(), event.getDurationDays(), open, close, variantTos, carTos));
        }

        Club club = clubRepository.findById(championship.getClubId()).orElse(null);
        return new ChampionshipDetailTo(
                championship.getId(),
                championship.getClubId(),
                club == null ? null : club.getName(),
                championship.getName(),
                championship.getStartsAt(),
                championship.getStatus().name(),
                championshipService.isOwner(championship, viewerId),
                eventTos);
    }

    private EventVariantTo toEventVariantTo(EventVariant ev, Map<UUID, Variant> variants,
                                            Map<UUID, Stage> stages, Map<UUID, String> locationNames) {
        Variant variant = variants.get(ev.getVariantId());
        String label;
        String stageName = null;
        String locationName = null;
        if (variant == null) {
            label = "(unknown variant)";
        } else {
            label = variant.getDisplayName() != null ? variant.getDisplayName() : variant.getRawName();
            Stage stage = variant.getStageId() == null ? null : stages.get(variant.getStageId());
            if (stage != null) {
                stageName = stage.getName();
                if (stage.getLocationId() != null) {
                    locationName = locationNames.get(stage.getLocationId());
                }
            }
        }
        return new EventVariantTo(ev.getVariantId(), ev.getPosition(), label, stageName, locationName);
    }

    private CarTo toCarTo(Car car) {
        return new CarTo(car.getId(), car.getName(), car.getYear(), car.getGroupName(),
                car.getClassName(), car.getCreatedAt(), car.getUpdatedAt());
    }

    private UUID championshipIdOf(UUID eventId) {
        return eventRepository.findById(eventId)
                .map(ChampionshipEvent::getChampionshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event."));
    }

    private UUID requireLogin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return principal.getId();
    }
}
