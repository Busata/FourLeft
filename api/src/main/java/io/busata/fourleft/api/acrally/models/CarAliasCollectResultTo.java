package io.busata.fourleft.api.acrally.models;

import java.util.List;

/** The outcome of collecting car aliases from results: how many were added, plus the full list. */
public record CarAliasCollectResultTo(int added, List<CarAliasTo> aliases) {
}
