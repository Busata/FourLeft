package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One DNF on an event, as the club owner's panel shows it. A DNF spends the driver's one shot at
 * that stage; the owner can revert it (see {@code POST /events/{eventId}/dnfs/{armId}/revert}) when
 * it was a technical mishap rather than a bail-out.
 */
public record EventDnfTo(
        /** The arm that ended as a DNF — the id to revert. */
        UUID armId,
        UUID userId,
        String driver,
        UUID variantId,
        String stageLabel,
        /** ABANDONED (run restarted/quit/crashed) or EXPIRED (armed, never ran). */
        String cause,
        LocalDateTime occurredAt,
        /** Set once an owner handed the shot back; null while the DNF still stands. */
        LocalDateTime revertedAt,
        String revertedBy,
        /** The driver has since set a time on that stage — a granted retry that was used. */
        boolean hasTimeSince) {
}
