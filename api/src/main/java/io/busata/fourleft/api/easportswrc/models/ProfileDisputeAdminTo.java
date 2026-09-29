package io.busata.fourleft.api.easportswrc.models;

import java.time.LocalDateTime;

/** An open dispute on the operator page: who holds the racenet account and who disputes it. */
public record ProfileDisputeAdminTo(
        String playerId,
        String racenet,
        String displayName,
        DiscordUserInfoTo holder,
        DiscordUserInfoTo disputer,
        LocalDateTime disputedAt) {
}
