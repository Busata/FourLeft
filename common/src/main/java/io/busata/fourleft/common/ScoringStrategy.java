package io.busata.fourleft.common;

/**
 * How custom championship points are derived from a finishing position. Extensible: today only a
 * sparse lookup table exists, but new strategies (e.g. linear falloff, FIA-style) can be added here.
 */
public enum ScoringStrategy {
    /** Finishing position -> points via a stored table; any position not present scores 1. */
    LOOKUP_TABLE,
    /**
     * Finishing position -> points via a compact ordered list of anchors and decreases. An anchor pins a
     * position to an absolute value; a decrease subtracts a (possibly fractional) amount per position until
     * the next entry; positions covered by neither score the configured floor (default 1). Reproduces a
     * full lookup table in a handful of hand-editable entries.
     */
    POINT_ANCHOR,
    /**
     * Racenet's own default (participation-scaled) system, reverse-engineered from club standings:
     * with P entrants, position r scores {@code max(0, floor(P * (3r + 1) / (4r)) - (r - 1))} — the
     * winner gets exactly P points, points fall to a linear ...3, 2, 1 tail and roughly the bottom
     * quarter of the field scores 0. Depends on the event's field size, so there is no configuration
     * payload to edit.
     */
    RACENET_DEFAULT
}
