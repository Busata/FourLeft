package io.busata.fourleft.api.acrally.models;

import java.util.List;

/** The outcome of a collect run: how many new stages were catalogued, plus the full refreshed list. */
public record StageNameCollectResultTo(
        int added,
        List<StageNameTo> stages) {
}
