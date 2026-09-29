package io.busata.fourleft.api.easportswrc.models;

import java.util.UUID;

/**
 * The outcome of /wrc profile or a claim/dispute; {@code requestId} is the private page link, null when there is
 * nothing to open. {@code playerId}/{@code racenet} name the racenet account the outcome is about (null for PICK).
 */
public record ProfileUpdateRequestResultTo(UUID requestId, Outcome outcome, String playerId, String racenet) {

    public enum Outcome {
        /** The racenet account is linked to this discord user; the link edits its profile. */
        LINKED,
        /** No racenet name given (and no profile yet), or the name wasn't found; the link lets them pick one. */
        PICK,
        /** Someone else holds this racenet account; the user can confirm to dispute it. Nothing changed yet. */
        CONFIRM_DISPUTE,
        /** The user disputes this racenet account (just now, or already did); the holder keeps it until resolved. */
        DISPUTED,
        /** Someone else holds this racenet account and another user already disputes it. */
        ALREADY_DISPUTED,
        /** The user already has an open dispute on another racenet account; one at a time. */
        DISPUTE_LIMIT,
        /** Someone else holds this racenet account and has verified it. */
        VERIFIED_BY_OTHER
    }
}
