package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.ChannelConfigurationTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleftdiscord.messages.templates.ClubSummaryResultTemplateResolver;
import io.busata.fourleftdiscord.messages.templates.CommunitySummaryResultTemplateResolver;
import io.busata.fourleftdiscord.messages.templates.MessageTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static io.busata.fourleftdiscord.messages.templates.MessageTemplate.messageTemplate;

@Component
@RequiredArgsConstructor
public class UserOverviewMessageFactory {
    private final ClubSummaryResultTemplateResolver clubSummaryResultTemplateResolver;
    private final CommunitySummaryResultTemplateResolver communitySummaryResultTemplateResolver;

    MessageTemplate COMMUNITY_ENTRY = messageTemplate("**${countryEmoticon}** • **${rank} / ${totalEntries}** • **${vehicleClass}** • ${totalTime} *(${totalDiff})*");
    MessageTemplate COMMUNITY_ENTRY_BADGE = messageTemplate("**${countryEmoticon}** • ${badgeRank} **${rank}** • **${vehicleClass}** • ${totalTime} *(${totalDiff})*");
    MessageTemplate CLUB_ENTRY = messageTemplate("${eventCountry} • **${rank}/${totalEntries}** • **${clubName}** • ${eventStage} • ${totalTime} *(${totalDiff})*");
    MessageTemplate CLUB_ENTRY_BADGE = messageTemplate("${eventCountry} • ${badgeRank} **${rank}** • **${clubName}** • ${eventStage} • ${totalTime} *(${totalDiff})*");


    MessageTemplate getCommunityEntryTemplate(boolean useBadges) {
        if(useBadges) {
            return COMMUNITY_ENTRY_BADGE;
        } else {
            return COMMUNITY_ENTRY;
        }
    }
    MessageTemplate getClubEntryTemplate(boolean useBadges) {
        if(useBadges) {
            return CLUB_ENTRY_BADGE;
        } else {
            return CLUB_ENTRY;
        }
    }
    public EmbedCreateSpec create(UserResultSummaryTo userResultSummaryTo, boolean useBadges) {

        final var communityEntryTemplate = getCommunityEntryTemplate(useBadges);
        final var clubEntryTemplate = getClubEntryTemplate(useBadges);

        final var builder = EmbedCreateSpec.builder();
        builder.title("**Personal results**");
        builder.color(Color.of(244, 0, 75));

        String activeCommunityEvent =
                userResultSummaryTo.communityActiveEventSummaries().stream()
                        .map(communityResultSummaryTo -> communitySummaryResultTemplateResolver.resolve(communityEntryTemplate, communityResultSummaryTo)).collect(Collectors.joining("\n"));

        String previousCommunityEvent =
                userResultSummaryTo.communityPreviousEventSummaries().stream()
                        .map(communityResultSummaryTo -> communitySummaryResultTemplateResolver.resolve(communityEntryTemplate, communityResultSummaryTo)).collect(Collectors.joining("\n"));

        String activeClubEvent =
                userResultSummaryTo.clubActiveEventSummaries().stream()
                        .map(clubResultSummaryTo ->  clubSummaryResultTemplateResolver.resolve(clubEntryTemplate, clubResultSummaryTo)).collect(Collectors.joining("\n"));

        String previousClubEvent = userResultSummaryTo.clubPreviousEventSummaries().stream()
                        .map(clubResultSummaryTo ->  clubSummaryResultTemplateResolver.resolve(clubEntryTemplate, clubResultSummaryTo)).collect(Collectors.joining("\n"));

        if(StringUtils.isNotBlank(activeCommunityEvent)) {
            builder.addField("Community challenges", activeCommunityEvent, false);
        }
        if(StringUtils.isNotBlank(previousCommunityEvent)) {
            builder.addField("Community challenges (yesterday)", previousCommunityEvent, false);
        }
        if(StringUtils.isNotBlank(activeClubEvent)) {
            builder.addField("Club events", activeClubEvent, false);
        }
        if(StringUtils.isNotBlank(previousClubEvent)) {
            builder.addField("Previous club events", previousClubEvent, false);
        }

        return builder.build();
    }
}
