package io.busata.fourleft.api.acrally.models;

import java.util.List;

/** The outcome of collecting track aliases from sessions: how many were added, plus the full list. */
public record TrackAliasCollectResultTo(int added, List<TrackAliasTo> aliases) {
}
