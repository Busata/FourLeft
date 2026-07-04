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
    POINT_ANCHOR
}
