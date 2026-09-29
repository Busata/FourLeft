package io.busata.fourleft.api.easportswrc.models;

import java.time.LocalDateTime;

/** A racenet account (by ssid) someone disputes, as shown to the disputer. */
public record ProfileDisputeTo(String playerId, String racenet, LocalDateTime disputedAt) {
}
