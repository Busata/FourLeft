package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** Create/update payload for a stage. {@code locationId} may be null (unassigned). */
public record StageRequestTo(String name, UUID locationId) {
}
