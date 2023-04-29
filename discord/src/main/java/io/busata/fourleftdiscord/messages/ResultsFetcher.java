package io.busata.fourleftdiscord.messages;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.common.ViewType;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleftdiscord.client.FourLeftClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResultsFetcher {
    private final MessageTemplateFactory messageTemplateFactory;
    private final FourLeftClient api;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;

    public List<EmbedCreateSpec> getCurrentEventResultsByChannelId(Snowflake channelId, ViewType viewType) {
        final UUID viewId = discordChannelConfigurationService.getViewId(channelId);
        return this.getCurrentEventResultsByViewId(viewId, viewType);
    }

    public List<EmbedCreateSpec> getCurrentEventResultsByViewId(UUID viewId, ViewType viewType) {
        final var clubResult = api.getViewCurrentResults(viewId);
        return switch (viewType) {
            case STANDARD -> messageTemplateFactory.createEmbedFromClubResult(clubResult);
            case EXTRA -> messageTemplateFactory.createExtraEmbedFromClubResult(clubResult);
        };
    }

    public List<EmbedCreateSpec> getPreviousEventResults(Snowflake channelId) {
        final UUID viewId = discordChannelConfigurationService.getViewId(channelId);
        final var clubResult = api.getViewPreviousResults(viewId);
        return messageTemplateFactory.createEmbedFromClubResult(clubResult);
    }

    public EmbedCreateSpec getChampionshipStandingsMessage(Snowflake channelId) {
        final UUID viewId = discordChannelConfigurationService.getViewId(channelId);
        final var pointResult = api.getViewCurrentStanding(viewId);
        return messageTemplateFactory.createEmbedFromStandingEntries(pointResult);
    }

    public List<EmbedCreateSpec> getPowerstageResults(Snowflake channelId) {
        final UUID viewId = discordChannelConfigurationService.getViewId(channelId);

        final var clubResult = api.getViewCurrentResults(viewId);
        return messageTemplateFactory.createPowerstageEmbed(clubResult);
    }

    public EmbedCreateSpec getClubMemberStatsMessage(Snowflake channelId) {
        final var viewId = discordChannelConfigurationService.getViewId(channelId);

        final var members = api.getMembers(viewId);
        return messageTemplateFactory.createEmbedFromMembers(members);
    }

    public EmbedCreateSpec getEventSummary(Snowflake channelId) {
        final var viewId = discordChannelConfigurationService.getViewId(channelId);

        final var summary = api.getViewEventSummary(viewId);
        return messageTemplateFactory.createEmbedFromSummary(summary);
    }

    public List<EmbedCreateSpec> getCommunityEventMessages() {
        List<CommunityChallengeSummaryTo> communityResults = api.getCommunityResults();
        return messageTemplateFactory.createEmbedFromCommunityEventResults(communityResults);
    }

}
