package io.busata.fourleft.application.discord;

import io.busata.fourleft.domain.discord.DiscordGuildMember;
import io.busata.fourleft.domain.discord.DiscordGuildMemberRepository;
import io.busata.fourleft.infrastructure.clients.discord.bot.DiscordBotClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordMemberUpdater {
    private final DiscordBotClient discordBotClient;
    private final DiscordGuildMemberRepository guildMemberRepository;
    private static final int memberLimit = 100;

    @Transactional
    public void sync() {

        discordBotClient.getGuilds().forEach(guild -> {
            log.info("Syncing discord guild members for {}", guild.name());
            guildMemberRepository.deleteByGuildId(guild.id());

            boolean keepFetching = true;
            String after = null;
            List<DiscordGuildMember> members = new ArrayList<>();

            while(keepFetching) {

                final var fetchedMembers =  this.discordBotClient.getMembers(guild.id(), memberLimit, after);

                if(fetchedMembers.size() < memberLimit) {
                    keepFetching = false;
                } else {
                    after = fetchedMembers.get(fetchedMembers.size() - 1).user().id();
                }

                List<DiscordGuildMember> list = fetchedMembers.stream().map(memberTo -> {
                    return new DiscordGuildMember(memberTo.user().id(), memberTo.user().username(), guild.id());
                }).toList();

                members.addAll(list);
            }

            log.info("Saving {} members for {}", members.size(), guild.name());
            guildMemberRepository.saveAll(members);



        });

    }
}
