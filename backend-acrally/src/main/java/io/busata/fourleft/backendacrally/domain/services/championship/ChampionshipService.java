package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventCar;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.models.club.Club;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
import io.busata.fourleft.backendacrally.domain.services.club.ClubRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Championship scheduling for clubs. Every mutation is restricted to the owner of the championship's
 * club (the club's {@code createdBy}); reads are left to the endpoint, which also decides draft
 * visibility. Events, their variants and their cars are managed with replace-list semantics.
 */
@Service
@RequiredArgsConstructor
public class ChampionshipService {

    private static final int MAX_NAME_LENGTH = 120;
    private static final int MAX_DURATION_DAYS = 366;
    private static final int MAX_GAP_DAYS = 366;

    private final ChampionshipRepository championshipRepository;
    private final ChampionshipEventRepository eventRepository;
    private final EventVariantRepository eventVariantRepository;
    private final EventCarRepository eventCarRepository;
    private final ClubRepository clubRepository;
    private final VariantRepository variantRepository;
    private final CarRepository carRepository;

    // --- Championship ---------------------------------------------------------------------------

    @Transactional
    public Championship create(UUID clubId, UUID userId, String rawName, LocalDateTime startsAt) {
        requireOwnedClub(clubId, userId);
        String name = requireName(rawName);
        if (startsAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A start date and time are required.");
        }
        return championshipRepository.save(new Championship(clubId, name, startsAt, userId));
    }

    @Transactional
    public Championship update(UUID championshipId, UUID userId, String rawName, LocalDateTime startsAt, String rawStatus) {
        Championship championship = requireOwnedChampionship(championshipId, userId);
        String name = requireName(rawName);
        if (startsAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A start date and time are required.");
        }
        championship.update(name, startsAt, parseStatus(rawStatus));
        return championship;
    }

    @Transactional
    public void delete(UUID championshipId, UUID userId) {
        Championship championship = requireOwnedChampionship(championshipId, userId);
        // Child events, variants and cars fall away via ON DELETE CASCADE.
        championshipRepository.delete(championship);
    }

    // --- Events ---------------------------------------------------------------------------------

    @Transactional
    public ChampionshipEvent addEvent(UUID championshipId, UUID userId, Integer gapDays,
                                      Integer durationDays, List<UUID> variantIds, List<UUID> carIds) {
        requireOwnedChampionship(championshipId, userId);
        int position = (int) eventRepository.countByChampionshipId(championshipId);
        ChampionshipEvent event = eventRepository.save(new ChampionshipEvent(
                championshipId, position, cleanGap(gapDays), cleanDuration(durationDays)));
        // Set the stages and cars up front so the owner never has to re-open the event to finish it.
        replaceEventVariants(event.getId(), variantIds);
        replaceEventCars(event.getId(), carIds);
        return event;
    }

    @Transactional
    public ChampionshipEvent updateEvent(UUID eventId, UUID userId, Integer gapDays, Integer durationDays) {
        ChampionshipEvent event = requireOwnedEvent(eventId, userId);
        event.update(cleanGap(gapDays), cleanDuration(durationDays));
        return event;
    }

    @Transactional
    public void deleteEvent(UUID eventId, UUID userId) {
        ChampionshipEvent event = requireOwnedEvent(eventId, userId);
        UUID championshipId = event.getChampionshipId();
        eventRepository.delete(event);
        eventRepository.flush();
        // Compact positions so the running order stays 0..n-1 with no gaps.
        List<ChampionshipEvent> remaining = eventRepository.findAllByChampionshipIdOrderByPositionAsc(championshipId);
        for (int i = 0; i < remaining.size(); i++) {
            if (remaining.get(i).getPosition() != i) {
                remaining.get(i).setPosition(i);
            }
        }
    }

    @Transactional
    public void reorderEvents(UUID championshipId, UUID userId, List<UUID> orderedEventIds) {
        requireOwnedChampionship(championshipId, userId);
        List<ChampionshipEvent> events = eventRepository.findAllByChampionshipIdOrderByPositionAsc(championshipId);
        Set<UUID> existing = events.stream().map(ChampionshipEvent::getId).collect(java.util.stream.Collectors.toSet());
        List<UUID> ordered = orderedEventIds == null ? List.of() : orderedEventIds;
        if (ordered.size() != events.size() || !existing.containsAll(ordered)
                || new LinkedHashSet<>(ordered).size() != ordered.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The new order must list each of the championship's events exactly once.");
        }
        // Two passes past a temporary offset to avoid tripping the unique (championship, position) index mid-update.
        for (ChampionshipEvent event : events) {
            event.setPosition(event.getPosition() + events.size());
        }
        eventRepository.flush();
        java.util.Map<UUID, ChampionshipEvent> byId = events.stream()
                .collect(java.util.stream.Collectors.toMap(ChampionshipEvent::getId, e -> e));
        for (int i = 0; i < ordered.size(); i++) {
            byId.get(ordered.get(i)).setPosition(i);
        }
    }

    // --- Event variants & cars ------------------------------------------------------------------

    @Transactional
    public void setEventVariants(UUID eventId, UUID userId, List<UUID> variantIds) {
        requireOwnedEvent(eventId, userId);
        replaceEventVariants(eventId, variantIds);
    }

    @Transactional
    public void setEventCars(UUID eventId, UUID userId, List<UUID> carIds) {
        requireOwnedEvent(eventId, userId);
        replaceEventCars(eventId, carIds);
    }

    /** Replace an event's ordered variants. Caller must have already verified ownership. */
    private void replaceEventVariants(UUID eventId, List<UUID> variantIds) {
        List<UUID> ids = dedupePreservingOrder(variantIds);
        for (UUID variantId : ids) {
            if (!variantRepository.existsById(variantId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A selected variant does not exist.");
            }
        }
        eventVariantRepository.deleteByEventId(eventId);
        eventVariantRepository.flush();
        List<EventVariant> rows = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            rows.add(new EventVariant(eventId, ids.get(i), i));
        }
        eventVariantRepository.saveAll(rows);
    }

    /** Replace an event's permitted cars. Caller must have already verified ownership. */
    private void replaceEventCars(UUID eventId, List<UUID> carIds) {
        List<UUID> ids = dedupePreservingOrder(carIds);
        for (UUID carId : ids) {
            if (!carRepository.existsById(carId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A selected car does not exist.");
            }
        }
        eventCarRepository.deleteByEventId(eventId);
        eventCarRepository.flush();
        List<EventCar> rows = ids.stream().map(carId -> new EventCar(eventId, carId)).toList();
        eventCarRepository.saveAll(rows);
    }

    // --- Reads ----------------------------------------------------------------------------------

    public List<Championship> listForClub(UUID clubId) {
        return championshipRepository.findAllByClubIdOrderByStartsAtAsc(clubId);
    }

    public Championship get(UUID championshipId) {
        return championshipRepository.findById(championshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such championship."));
    }

    /** True when the given user owns the championship's club. */
    public boolean isOwner(Championship championship, UUID userId) {
        return clubRepository.findById(championship.getClubId())
                .map(club -> club.getCreatedBy().equals(userId))
                .orElse(false);
    }

    // --- Guards & helpers -----------------------------------------------------------------------

    private Club requireOwnedClub(UUID clubId, UUID userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such club."));
        if (!club.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the club owner can manage championships.");
        }
        return club;
    }

    private Championship requireOwnedChampionship(UUID championshipId, UUID userId) {
        Championship championship = get(championshipId);
        requireOwnedClub(championship.getClubId(), userId);
        return championship;
    }

    private ChampionshipEvent requireOwnedEvent(UUID eventId, UUID userId) {
        ChampionshipEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event."));
        requireOwnedChampionship(event.getChampionshipId(), userId);
        return event;
    }

    private String requireName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A name is required.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Name must be at most %d characters.".formatted(MAX_NAME_LENGTH));
        }
        return name;
    }

    private int cleanGap(Integer gapDays) {
        int gap = gapDays == null ? 0 : gapDays;
        if (gap < 0 || gap > MAX_GAP_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Gap must be between 0 and %d days.".formatted(MAX_GAP_DAYS));
        }
        return gap;
    }

    private int cleanDuration(Integer durationDays) {
        int duration = durationDays == null ? 7 : durationDays;
        if (duration < 1 || duration > MAX_DURATION_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Duration must be between 1 and %d days.".formatted(MAX_DURATION_DAYS));
        }
        return duration;
    }

    private ChampionshipStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return ChampionshipStatus.DRAFT;
        }
        try {
            return ChampionshipStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status '%s'.".formatted(rawStatus));
        }
    }

    private List<UUID> dedupePreservingOrder(List<UUID> ids) {
        if (ids == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }
}
