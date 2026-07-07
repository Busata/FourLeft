package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** Assigns the readable display name (blank clears it) and the stage for a variant. */
public record UpdateVariantRequestTo(String displayName, UUID stageId) {
}
