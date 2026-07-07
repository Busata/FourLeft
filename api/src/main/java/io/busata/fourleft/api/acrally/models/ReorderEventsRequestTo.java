package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/** Sets the running order of a championship's events; the list order is the new order. */
public record ReorderEventsRequestTo(
        List<UUID> eventIds) {
}
