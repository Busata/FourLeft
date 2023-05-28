package io.busata.fourleft.endpoints.discord;

import io.busata.fourleft.api.models.discord.DiscordGuildMemberTo;
import io.busata.fourleft.domain.discord.DiscordGuildMember;
import io.busata.fourleft.infrastructure.common.Factory;

@Factory
public class DiscordGuildMemberFactory {

    public DiscordGuildMemberTo create(DiscordGuildMember member) {
        return new DiscordGuildMemberTo(
                member.getId(),
                member.getUserName()
        );
    }
}
