package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** Manually add a track alias: the exact string telemetry reports, optionally assigned to a variant. */
public record CreateTrackAliasRequestTo(String rawName, UUID variantId) {
}
