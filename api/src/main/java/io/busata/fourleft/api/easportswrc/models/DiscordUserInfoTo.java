package io.busata.fourleft.api.easportswrc.models;

/**
 * A discord user as the operator pages show them. Only {@code id} is guaranteed: the names and avatar are looked
 * up live and null when that fails.
 */
public record DiscordUserInfoTo(String id, String username, String globalName, String avatarUrl) {
}
