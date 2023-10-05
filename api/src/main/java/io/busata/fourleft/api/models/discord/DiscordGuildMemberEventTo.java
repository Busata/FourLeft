package io.busata.fourleft.api.models.discord;

import io.busata.fourleft.common.MemberEvent;

public record DiscordGuildMemberEventTo(String discordId, String username, MemberEvent memberEvent) {
}
