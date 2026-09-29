package io.busata.fourleft.api.easportswrc.models;

/**
 * What a profile link opens: the linked profile, or null when the user still has to pick their racenet name;
 * plus the racenet account the user disputes, if any (they can withdraw it).
 */
public record ProfilePageTo(ProfileTo profile, ProfileDisputeTo disputing) {
}
