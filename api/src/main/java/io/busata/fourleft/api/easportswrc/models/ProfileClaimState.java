package io.busata.fourleft.api.easportswrc.models;

/**
 * How much we trust a profile's discord ↔ racenet link. Claims are self-service; verification (proving control
 * of the racenet account) comes later.
 */
public enum ProfileClaimState {
    /** Linked by the discord user themselves, unchallenged. */
    CLAIMED,
    /** Proven: the holder showed they control the racenet account. Other claims no longer affect it. */
    VERIFIED,
    /** A second discord user claimed the same racenet account; the first holder keeps it until resolved. */
    DISPUTED
}
