package io.busata.fourleft.common;

/**
 * What an event restriction rule checks on a leaderboard entry. Extensible: today only a vehicle
 * allowlist exists, but new checks (e.g. platform, assists) can be added here.
 */
public enum RestrictionType {
    /** Entry's vehicle must exactly match one of the rule's allowed vehicle names. */
    VEHICLE_ALLOWLIST
}
