package io.busata.fourleft.api.acrally.models;

import java.util.List;

/** The outcome of a collect run: how many new variants were catalogued, plus the full refreshed list. */
public record VariantCollectResultTo(
        int added,
        List<VariantTo> variants) {
}
