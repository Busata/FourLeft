package io.busata.fourleft.backendacrally.domain.services.championship;

import io.busata.fourleft.backendacrally.domain.models.championship.EventArm;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmOutcome;
import io.busata.fourleft.backendacrally.domain.models.championship.EventArmStatus;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantService;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The club owner's view of an event's DNFs, and the one lever they have over them: a revert.
 *
 * <p>A DNF spends the driver's one shot at a stage on purpose — restarting or quitting a bad run
 * has to cost something, or the one-shot rule means nothing. But the server can't tell a bail-out
 * from a crashed game, a killed agent, or an arm someone pressed by accident, so the club owner
 * gets the final say: reverting hands the shot back without erasing the record of what happened.
 *
 * <p>Unlike {@link EventArmService} (personal, driver-scoped), every method here is moderation-
 * scoped: the caller must own the club the event's championship belongs to, or be a platform admin.
 */
@Service
@RequiredArgsConstructor
public class EventDnfService {

    private final EventArmRepository armRepository;
    private final EventEntryRepository entryRepository;
    private final AppUserRepository appUserRepository;
    private final ChampionshipService championshipService;
    private final VariantService variantService;

    /** Why a stage ended without a time. */
    public enum DnfCause {
        /** A run was under way and never produced a save record — restarted, quit, or crashed. */
        ABANDONED,
        /** The arm sat waiting until the janitor timed it out; no run was ever bound to it. */
        EXPIRED
    }

    /**
     * One DNF as the owner sees it. {@code revertedAt}/{@code revertedBy} are null while the DNF
     * still stands; {@code hasTimeSince} means the driver already used a granted retry and set a
     * time, so there is nothing left to hand back.
     */
    public record DnfRow(UUID armId, UUID userId, String driver, UUID variantId, String stageLabel,
                         DnfCause cause, LocalDateTime occurredAt,
                         LocalDateTime revertedAt, String revertedBy, boolean hasTimeSince) {
    }

    /** Every DNF on an event, freshest first — reverted ones included, marked as handled. */
    public List<DnfRow> list(UUID eventId, UUID actorId, boolean admin) {
        championshipService.requireModeratableEvent(eventId, actorId, admin);
        List<EventArm> dnfs =
                armRepository.findAllByEventIdAndOutcomeOrderByUpdatedAtDesc(eventId, EventArmOutcome.DNF);
        if (dnfs.isEmpty()) {
            return List.of();
        }

        Map<UUID, String> names = resolveNames(dnfs);
        Map<UUID, VariantService.VariantLabel> labels = variantService.labelsById();
        // Stages the driver has since put a time on — a reverted DNF that was actually re-run.
        java.util.Set<String> timed = entryRepository.findByEventId(eventId).stream()
                .map(entry -> key(entry.getUserId(), entry.getVariantId()))
                .collect(Collectors.toSet());

        return dnfs.stream()
                .map(arm -> toRow(arm, names, labels, timed.contains(key(arm.getUserId(), arm.getVariantId()))))
                .toList();
    }

    /**
     * Hand the driver's shot at that stage back. The arm keeps its DNF outcome (and shows in the
     * panel as reverted, by whom); it simply stops counting against them, so they can arm the stage
     * again while the event's window is still open. Idempotent — reverting twice changes nothing.
     * {@code actorId} is stamped on the arm, so the panel names whoever granted the retry —
     * club owner or admin.
     */
    @Transactional
    public void revert(UUID eventId, UUID armId, UUID actorId, boolean admin) {
        championshipService.requireModeratableEvent(eventId, actorId, admin);
        EventArm arm = armRepository.findById(armId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such DNF."));
        if (!arm.getEventId().equals(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such DNF.");
        }
        if (arm.getOutcome() != EventArmOutcome.DNF) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That run didn't end as a DNF.");
        }
        if (arm.isReverted()) {
            return;
        }
        arm.revert(actorId);
    }

    private DnfRow toRow(EventArm arm, Map<UUID, String> names,
                         Map<UUID, VariantService.VariantLabel> labels, boolean hasTimeSince) {
        VariantService.VariantLabel label = labels.get(arm.getVariantId());
        DnfCause cause = arm.getStatus() == EventArmStatus.EXPIRED ? DnfCause.EXPIRED : DnfCause.ABANDONED;
        return new DnfRow(
                arm.getId(),
                arm.getUserId(),
                names.getOrDefault(arm.getUserId(), "—"),
                arm.getVariantId(),
                label == null ? "(stage)" : label.fullLabel(),
                cause,
                arm.resolvedAt(),
                arm.getRevertedAt(),
                arm.getRevertedBy() == null ? null : names.get(arm.getRevertedBy()),
                hasTimeSince);
    }

    /** Display names of everyone the panel mentions: the drivers plus whoever reverted. */
    private Map<UUID, String> resolveNames(List<EventArm> dnfs) {
        List<UUID> ids = Stream.concat(
                        dnfs.stream().map(EventArm::getUserId),
                        dnfs.stream().map(EventArm::getRevertedBy).filter(Objects::nonNull))
                .distinct()
                .toList();
        return appUserRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getDisplayName));
    }

    private String key(UUID userId, UUID variantId) {
        return userId + ":" + variantId;
    }
}
