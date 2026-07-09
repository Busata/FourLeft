package io.busata.fourleft.common;

/**
 * How a restriction violation affects displayed results (web overview, exports, Discord posts).
 */
public enum RestrictionDisplayMode {
    /** Violating entries stay in the results but carry a warning marker. */
    WARN,
    /** Violating entries are removed from the results; remaining entries are re-ranked for display. */
    EXCLUDE
}
