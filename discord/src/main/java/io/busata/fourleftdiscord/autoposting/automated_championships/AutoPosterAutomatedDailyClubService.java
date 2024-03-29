package io.busata.fourleftdiscord.autoposting.automated_championships;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.common.ViewType;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleft.common.MessageType;
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
            log.error("Something went wrong posting the results", ex);
        }
    }

    public void postChampionship(Snowflake channelId) {
        EmbedCreateSpec message = resultsFetcher.getChampionshipStandingsMessage(channelId);
        try {
            discordUtils.postMessage(
                    channelId,
                    message.withTitle("Championship standings"),
                    MessageType.AUTOMATED_CLUB_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong posting the championship results", ex);
        }
    }

    public void postNewStage(Snowflake channelId) {
        try {

            List<EmbedCreateSpec> messages = resultsFetcher.getCurrentEventResultsByChannelId(channelId, ViewType.STANDARD);

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


