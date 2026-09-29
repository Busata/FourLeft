package io.busata.fourleft.api.easportswrc.models;

/** Claim (or, after confirming, dispute) a racenet account by ssid, as returned by the player suggestions. */
public record ProfileClaimRequestTo(String playerId) {
}
