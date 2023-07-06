package io.busata.fourleft.api.events;

import io.busata.fourleft.api.models.FIATickerUpdateTo;

import java.util.List;

public record FIATickerUpdateEvent(List<FIATickerUpdateTo> updates) {
}
