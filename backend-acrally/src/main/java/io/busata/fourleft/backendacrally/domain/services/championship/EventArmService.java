package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.Championship;
import io.busata.fourleft.backendacrally.domain.models.championship.ChampionshipEvent;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import io.busata.fourleft.backendacrally.domain.models.championship.EventVariant;
import io.busata.fourleft.backendacrally.domain.services.club.ClubMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The driver-facing side of the Start button: a driver arms a specific stage (variant) of an event,
 * and the arm binds to their next run (see {@code SessionIngestService}). Every method is scoped to
 * the calling user — arms are personal.
 */
@Service
@RequiredArgsConstructor
public class EventArmService {

    private static final List<EventArmStatus> LIVE = List.of(EventArmStatus.ARMED, EventArmStatus.BOUND);

    private final EventArmRepository armRepository;
    private final ChampionshipEventRepository eventRepository;
    private final EventVariantRepository eventVariantRepository;
    private final ChampionshipRepository championshipRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ChampionshipService championshipService;

    /**
     * Arm a stage: replace any existing live arm and start waiting for the driver's next run. The
     * event must be open (published + within its window) and the variant must be one it runs, and the
     * driver must belong to the club. Returns the fresh {@code ARMED} arm.
     */
    @Transactional
    public EventArm arm(UUID userId, UUID eventId, UUID variantId) {
        ChampionshipEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event."));
        Championship championship = championshipRepository.findById(event.getChampionshipId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such event."));
        if (!membershipRepository.existsByClubIdAndUserId(championship.getClubId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Join the club to enter its events.");
        }
        if (!championshipService.isOpen(eventId, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This event isn't open for entries right now.");
        }
        boolean variantInEvent = eventVariantRepository.findAllByEventIdOrderByPositionAsc(eventId).stream()
                .map(EventVariant::getVariantId)
                .anyMatch(variantId::equals);
        if (!variantInEvent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That stage isn't part of this event.");
        }

        cancelLive(userId);
        return armRepository.save(new EventArm(userId, eventId, variantId));
    }

    /** Cancel the driver's live arm, if any. Idempotent. */
    @Transactional
    public void disarm(UUID userId) {
        cancelLive(userId);
    }

    /** The driver's current live arm (ARMED or BOUND), if any. */
    public Optional<EventArm> liveArm(UUID userId) {
        return armRepository.findFirstByUserIdAndStatusIn(userId, LIVE);
    }

    /** The driver's most recent arm regardless of status — for surfacing the last run's outcome. */
    public Optional<EventArm> latestArm(UUID userId) {
        return armRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Cancel the live arm and flush, so a subsequent insert doesn't collide on the live-arm index. */
    private void cancelLive(UUID userId) {
        armRepository.findFirstByUserIdAndStatusIn(userId, LIVE).ifPresent(arm -> {
            arm.cancel();
            armRepository.saveAndFlush(arm);
        });
    }
}
