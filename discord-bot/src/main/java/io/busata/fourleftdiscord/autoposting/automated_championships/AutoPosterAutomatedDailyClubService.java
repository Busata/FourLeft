package io.busata.fourleftdiscord.autoposting.automated_championships;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleft.domain.discord.models.MessageType;
import io.busata.fourleftdiscord.messages.ResultsFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPosterAutomatedDailyClubService {
    private final ResultsFetcher resultsFetcher;
    private final DiscordMessageGateway discordUtils;

    public void postResults(Snowflake channelId) {
        List<EmbedCreateSpec> message = resultsFetcher.getPreviousEventResults(channelId);


        try {
            discordUtils.postMessage(
                    channelId,
                    message,
                            MessageType.AUTOMATED_CLUB_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong posting the weekly results", ex);
        }
    }

    public void postChampionship(Snowflake channelId) {
        EmbedCreateSpec message = resultsFetcher.getChampionshipStandingsMessage(channelId);
        try {
            discordUtils.postMessage(
                    channelId,
                    message.withTitle("Daily Championship standings"),
                    MessageType.AUTOMATED_CLUB_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong posting the weekly results", ex);
        }
    }

    public void postNewStage(Snowflake channelId) {
        List<EmbedCreateSpec> messages = resultsFetcher.getCurrentEventResults(channelId);

            try {
                discordUtils.postMessage(
                        channelId,
                        messages,
                        MessageType.AUTOMATED_CLUB_POST
                );
            } catch (Exception ex) {
                log.error("Something went wrong posting the weekly results", ex);
            }
    }
}


