package io.busata.fourleft.api.events;

import io.busata.fourleft.api.models.WRCTickerUpdateTo;

import java.util.List;

public record WRCTickerUpdateEvent(List<WRCTickerUpdateTo> updates) {
}
