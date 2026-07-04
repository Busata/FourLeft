package io.busata.fourleft.common;

/**
 * How custom championship points are derived from a finishing position. Extensible: today only a
 * sparse lookup table exists, but new strategies (e.g. linear falloff, FIA-style) can be added here.
 */
public enum ScoringStrategy {
    /** Finishing position -> points via a stored table; any position not present scores 1. */
    LOOKUP_TABLE
}
