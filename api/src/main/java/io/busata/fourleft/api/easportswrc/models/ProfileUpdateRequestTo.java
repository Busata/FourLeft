package io.busata.fourleft.api.easportswrc.models;

/** {@code racenet} is optional: without it the user gets their own profile, or a page to pick their name. */
public record ProfileUpdateRequestTo(String racenet, String discordId, String userName) {
}
