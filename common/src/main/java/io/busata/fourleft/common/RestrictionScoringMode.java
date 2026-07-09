package io.busata.fourleft.common;

/**
 * How a restriction violation affects custom championship points. Only applies when custom scoring
 * is enabled on the channel configuration — racenet's own standings cannot be altered.
 */
public enum RestrictionScoringMode {
    /** Violators score 0 and compliant drivers below them move up into the vacated scoring positions. */
    EXCLUDE,
    /** Violators keep their position; a flat, configurable number of points is deducted (floored at 0). */
    PENALTY
}
