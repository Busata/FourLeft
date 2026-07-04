package io.busata.fourleft.backendeasportswrc.domain.models.scoring;

import java.util.List;

/**
 * A compact, hand-editable scoring definition for the {@code POINT_ANCHOR} strategy: an ordered list of
 * {@link ScoringAnchorEntry anchors and decreases} plus a {@code floor} (the points awarded to any position
 * not covered by an anchor or an active decrease — mirrors the old lookup table's default of 1). The
 * position -> points expansion lives in the scoring service, not here; this is pure persisted data.
 */
public record ScoringAnchors(int floor, List<ScoringAnchorEntry> entries) {
}
