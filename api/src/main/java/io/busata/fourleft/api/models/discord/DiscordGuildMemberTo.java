package io.busata.fourleft.api.models.discord;

import java.util.UUID;

public record DiscordGuildMemberTo(UUID id, String discordId, String username) {
}
