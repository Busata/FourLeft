package io.busata.fourleftdiscord.autoposting.automated_championships;

import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.common.ViewType;
import io.busata.fourleftdiscord.commands.DiscordChannels;
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
public class AutoPosterAutomatedMonthlyClubService {
    private final ResultsFetcher resultsFetcher;
    private final DiscordMessageGateway discordUtils;

    public void postResults() {
        List<EmbedCreateSpec> messages = resultsFetcher.getPreviousEventResults(DiscordChannels.DIRTY_MONTHLIES);


        try {
            discordUtils.postMessage(
                    DiscordChannels.DIRTY_MONTHLIES,
                    messages,
                            MessageType.AUTOMATED_CLUB_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong posting the weekly results", ex);
        }
    }

    public void postChampionship() {
        EmbedCreateSpec message = resultsFetcher.getChampionshipStandingsMessage(DiscordChannels.DIRTY_MONTHLIES);
        try {
            discordUtils.postMessage(
                    DiscordChannels.DIRTY_MONTHLIES,
                    message.withTitle("Monthly - Week event standings"),
                    MessageType.AUTOMATED_CLUB_POST
            );
        } catch (Exception ex) {
            log.error("Something went wrong posting the weekly results", ex);
        }
    }

    public void postNewStage() {
        List<EmbedCreateSpec> messages = resultsFetcher.getCurrentEventResultsByChannelId(DiscordChannels.DIRTY_MONTHLIES, ViewType.STANDARD);

            try {
                discordUtils.postMessage(
                        DiscordChannels.DIRTY_MONTHLIES,
                        messages,
                        MessageType.AUTOMATED_CLUB_POST
                );
            } catch (Exception ex) {
                log.error("Something went wrong posting the weekly results", ex);
            }
    }
}


