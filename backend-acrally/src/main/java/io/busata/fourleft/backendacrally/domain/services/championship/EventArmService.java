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
    private final EventEntryRepository entryRepository;
    private final ChampionshipEventRepository eventRepository;
    private final EventVariantRepository eventVariantRepository;
    private final ChampionshipRepository championshipRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ChampionshipService championshipService;

    /**
     * Arm a stage: replace any existing ARMED arm and start waiting for the driver's next run. The
     * event must be open (published + within its window) and the variant must be one it runs, and the
     * driver must belong to the club. Rejected (409) while a BOUND run is in progress — switching
     * stages mid-run would be a disarm by another name — and once the driver's one shot at the stage
     * is spent: a recorded time or a DNF expiry locks the stage (wrong-stage/wrong-car mishaps don't).
     * Returns the fresh {@code ARMED} arm.
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
        if (!championshipService.isOpenNow(eventId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This event isn't open for entries right now.");
        }
        boolean variantInEvent = eventVariantRepository.findAllByEventIdOrderByPositionAsc(eventId).stream()
                .map(EventVariant::getVariantId)
                .anyMatch(variantId::equals);
        if (!variantInEvent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That stage isn't part of this event.");
        }
        if (entryRepository.findByEventIdAndVariantIdAndUserId(eventId, variantId, userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You've already run this stage — one shot per stage, and your time is in.");
        }
        if (armRepository.existsByUserIdAndEventIdAndVariantIdAndStatus(
                userId, eventId, variantId, EventArmStatus.EXPIRED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Your entry on this stage expired as a DNF — one shot per stage.");
        }

        cancelLive(userId);
        return armRepository.save(new EventArm(userId, eventId, variantId));
    }

    /**
     * Cancel the driver's live arm, if any. Idempotent while ARMED; rejected while BOUND — once a
     * run is in progress its outcome must be recorded, or disarming becomes a cherry-picking tool
     * (bail out of a bad run before the finish and keep the old time). The run itself is the escape
     * hatch: restarting or quitting the stage aborts the session, which releases the arm.
     */
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

    /**
     * Janitor: expire ARMED arms with no activity since the cutoff, resolving them as DNF. An arm
     * binds to the driver's NEXT session, so one left waiting would otherwise capture whatever run
     * they happen to start days later. Only ARMED arms qualify: a BOUND arm belongs to a run in
     * progress, and the stale-session sweep unbinds it (back to ARMED, refreshing its activity)
     * if that run dies. Returns how many were expired (for the schedule's log).
     */
    @Transactional
    public int expireIdleArms(java.time.LocalDateTime cutoff) {
        List<EventArm> idle = armRepository.findArmedAndIdleSince(cutoff);
        idle.forEach(EventArm::expire);
        return idle.size();
    }

    /**
     * Cancel the live arm and flush, so a subsequent insert doesn't collide on the live-arm index.
     * A BOUND arm is never cancelled — not by disarm, not by arming another stage — so a run in
     * progress cannot be un-entered; it resolves via its session (result, abort, or the stale sweep).
     */
    private void cancelLive(UUID userId) {
        armRepository.findFirstByUserIdAndStatusIn(userId, LIVE).ifPresent(arm -> {
            if (arm.getStatus() == EventArmStatus.BOUND) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A run is in progress on your armed stage — it has to finish (or be restarted/quit) first.");
            }
            arm.cancel();
            armRepository.saveAndFlush(arm);
        });
    }
}
