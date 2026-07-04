package io.busata.fourleft.api.easportswrc.models;

import java.util.List;

/**
 * Wire model for the {@code POINT_ANCHOR} scoring strategy: an ordered list of anchors/decreases plus the
 * {@code floor} awarded to positions covered by neither (default 1).
 */
public record ScoringAnchorsTo(
        int floor,
        List<ScoringAnchorEntryTo> entries)
{
}
