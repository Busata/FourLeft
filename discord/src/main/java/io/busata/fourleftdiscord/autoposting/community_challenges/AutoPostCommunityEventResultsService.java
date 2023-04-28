package io.busata.fourleftdiscord.autoposting.community_challenges;

import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.commands.DiscordChannels;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleft.api.messages.MessageType;
import io.busata.fourleftdiscord.messages.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPostCommunityEventResultsService
{
    private final ResultsFetcher resultsFetcher;
    private final DiscordMessageGateway discordMessageGateway;

    public void update() {
        List<EmbedCreateSpec> messages = resultsFetcher.getCommunityEventMessages();

        messages.forEach(message -> {
            try {
                discordMessageGateway.postMessage(
                        DiscordChannels.DIRTY_MAIN_CHAT,
                        message,
                        MessageType.COMMUNITY_EVENT
                );
                Thread.sleep(1000);
            } catch (Exception ex) {
                log.error("Something went wrong posting the daily results", ex);
            }
        });
    }


}
