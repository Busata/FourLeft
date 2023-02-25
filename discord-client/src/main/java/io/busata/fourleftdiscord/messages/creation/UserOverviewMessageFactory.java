package io.busata.fourleftdiscord.messages.creation;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.busata.fourleft.api.models.ChannelConfigurationTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleftdiscord.helpers.ListHelpers;
import io.busata.fourleftdiscord.messages.templates.ClubSummaryResultTemplateResolver;
import io.busata.fourleftdiscord.messages.templates.CommunitySummaryResultTemplateResolver;
import io.busata.fourleftdiscord.messages.templates.MessageTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.busata.fourleftdiscord.messages.templates.MessageTemplate.messageTemplate;

@Component
@RequiredArgsConstructor
public class UserOverviewMessageFactory {
    private final ClubSummaryResultTemplateResolver clubSummaryResultTemplateResolver;
    private final CommunitySummaryResultTemplateResolver communitySummaryResultTemplateResolver;

    MessageTemplate COMMUNITY_ENTRY = messageTemplate("**${countryEmoticon}** • **${rank} / ${totalEntries}** • **${vehicleClass}** • ${totalTime} *(${totalDiff})*");
    MessageTemplate COMMUNITY_ENTRY_BADGE = messageTemplate("**${countryEmoticon}** • ${badgeRank} • **${rank}** • **${vehicleClass}** • ${totalTime} *(${totalDiff})*");
    MessageTemplate CLUB_ENTRY = messageTemplate("${eventCountry} • **${rank}/${totalEntries}** • **${clubName}** • ${eventStage} • ${totalTime} *(${totalDiff})*");
    MessageTemplate CLUB_ENTRY_BADGE = messageTemplate("${eventCountry} • ${badgeRank} • **${rank}** • **${clubName}** • ${eventStage} • ${totalTime} *(${totalDiff})*");


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
    public EmbedCreateSpec create(String username, UserResultSummaryTo userResultSummaryTo, boolean useBadges) {

        final var communityEntryTemplate = getCommunityEntryTemplate(useBadges);
        final var clubEntryTemplate = getClubEntryTemplate(useBadges);

        final var builder = EmbedCreateSpec.builder();
        builder.title("**Personal results • " + username + "**");
        builder.color(Color.of(244, 0, 75));

        List<String> activeCommunityEvent = ListHelpers.partitionInGroups(userResultSummaryTo.communityActiveEventSummaries(), 4)
                .stream()
                .map(eventSummaries -> {
                    return eventSummaries.stream()
                            .map(communityResultSummaryTo -> communitySummaryResultTemplateResolver.resolve(communityEntryTemplate, communityResultSummaryTo))
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining("\n"));

                }).toList();

        for (int i = 0; i < activeCommunityEvent.size(); i++) {
            String title = i == 0 ? "Community challenges" : "\u200b";
            String event = activeCommunityEvent.get(i);
            builder.addField(title, event, false);
        }

        List<String> previousCommunityEvents = ListHelpers.partitionInGroups(userResultSummaryTo.communityPreviousEventSummaries(), 4)
                .stream()
                .map(eventSummaries -> {
                    return eventSummaries.stream()
                            .map(communityResultSummaryTo -> communitySummaryResultTemplateResolver.resolve(communityEntryTemplate, communityResultSummaryTo)).collect(Collectors.joining("\n"));
                }).toList();

        for (int i = 0; i < previousCommunityEvents.size(); i++) {
            String title = i == 0 ? "Community challenges (yesterday)" : "\u200b";
            String event = previousCommunityEvents.get(i);
            builder.addField(title, event, false);
        }

        List<String> activeClubEvents = ListHelpers.partitionInGroups(userResultSummaryTo.clubActiveEventSummaries(), 4)
                .stream()
                .map(eventSummaries ->
                        eventSummaries.stream().map(clubResultSummaryTo -> clubSummaryResultTemplateResolver.resolve(clubEntryTemplate, clubResultSummaryTo)).collect(Collectors.joining("\n"))
                ).toList();

        for (int i = 0; i < activeClubEvents.size(); i++) {
            String title = i == 0 ? "Club Challenges" : "\u200b";
            String event = activeClubEvents.get(i);
            builder.addField(title, event, false);
        }

        List<String> previousClubEvent = ListHelpers.partitionInGroups(userResultSummaryTo.clubPreviousEventSummaries(), 4)
                .stream()
                .map(eventSummaries -> eventSummaries.stream().map(clubResultSummaryTo ->  clubSummaryResultTemplateResolver.resolve(clubEntryTemplate, clubResultSummaryTo)).collect(Collectors.joining("\n"))
                ).toList();

        for (int i = 0; i < previousClubEvent.size(); i++) {
            String title = i == 0 ? "\"Previous club challenges\"" : "\u200b";
            String event = previousClubEvent.get(i);
            builder.addField(title, event, false);
        }


        return builder.build();
    }
}
