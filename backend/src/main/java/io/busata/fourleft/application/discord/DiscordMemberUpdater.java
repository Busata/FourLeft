package io.busata.fourleft.application.discord;

import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.domain.discord.DiscordGuildMember;
import io.busata.fourleft.domain.discord.DiscordGuildMemberRepository;
import io.busata.fourleft.infrastructure.clients.discord.bot.DiscordBotClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordMemberUpdater {
    private final DiscordBotClient discordBotClient;
    private final DiscordGuildMemberRepository guildMemberRepository;
    private static final int memberLimit = 100;

    @Transactional
    public void sync() {
        discordBotClient.getGuilds().stream().map(DiscordGuildTo::id).forEach(this::syncGuild);
    }

    public void syncGuild(String guildId) {
        log.info("Syncing discord guild members for {}", guildId);
        guildMemberRepository.deleteByGuildId(guildId);

        boolean keepFetching = true;
        String after = null;
        List<DiscordGuildMember> members = new ArrayList<>();

        while(keepFetching) {

            final var fetchedMembers =  this.discordBotClient.getMembers(guildId, memberLimit, after);

            if(fetchedMembers.size() < memberLimit) {
                keepFetching = false;
            } else {
                after = fetchedMembers.get(fetchedMembers.size() - 1).user().id();
            }

            List<DiscordGuildMember> list = fetchedMembers.stream().map(memberTo -> {
                return new DiscordGuildMember(UUID.randomUUID(), memberTo.user().id(), memberTo.user().username(), guildId);
            }).toList();

            members.addAll(list);
        }

        log.info("Saving {} members for {}", members.size(), guildId);
        guildMemberRepository.saveAll(members);
    }
}
