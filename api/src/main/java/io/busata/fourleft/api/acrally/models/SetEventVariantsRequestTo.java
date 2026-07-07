package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/** Replaces an event's variants; the list order is the running order. */
public record SetEventVariantsRequestTo(
        List<UUID> variantIds) {
}
