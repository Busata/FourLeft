package io.busata.fourleft.application.discord;

import io.busata.fourleft.api.models.discord.DiscordGuildMemberTo;
import io.busata.fourleft.domain.discord.DiscordGuildMember;
import io.busata.fourleft.infrastructure.common.Factory;

@Factory
public class DiscordGuildMemberFactory {

    public DiscordGuildMemberTo create(DiscordGuildMember member) {
        return new DiscordGuildMemberTo(
                member.getId(),
                member.getDiscordId(),
                member.getUserName()
        );
    }
}
