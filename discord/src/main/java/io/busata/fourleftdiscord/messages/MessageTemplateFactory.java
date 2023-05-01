package io.busata.fourleftdiscord.messages;

import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableView;
import io.busata.fourleftdiscord.messages.creation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageTemplateFactory {
    private final ChampionshipStandingsMessageFactory championshipStandingsMessageFactory;
    private final ClubEventResultMessageFactory clubEventResultMessageFactory;
    private final ClubMembersMessageFactory clubMembersMessageFactory;
    private final UserOverviewMessageFactory userOverviewMessageFactory;
    private final EventSummaryMessageFactory eventSummaryMessageFactory;
    private final CommunityEventMessageFactory communityEventMessageFactory;

    private final AutoPostResultMessageFactory autoPostResultMessageFactory;

    public String createAutopostMessage(AutoPostableView view) {
        return autoPostResultMessageFactory.createAutopostMessage(view);
    }

    public List<EmbedCreateSpec> createEmbedFromClubResult(UUID viewId, ViewResultTo result) {
        return clubEventResultMessageFactory.createDefault(viewId, result);
    }
    public List<EmbedCreateSpec> createExtraEmbedFromClubResult(UUID viewId, ViewResultTo result) {
        return clubEventResultMessageFactory.createMetadata(viewId, result);
    }

    public EmbedCreateSpec createEmbedFromStandingEntries(ViewPointsTo result) {
        return championshipStandingsMessageFactory.create(result);
    }
    public List<EmbedCreateSpec> createPowerstageEmbed(UUID viewId, ViewResultTo clubResult) {
        return clubEventResultMessageFactory.createPowerStage(viewId, clubResult);
    }

    public List<EmbedCreateSpec> createEmbedFromCommunityEventResults(List<CommunityChallengeSummaryTo> events) {
        return communityEventMessageFactory.getEmbeds(events);
    }

    public EmbedCreateSpec createEmbedFromMembers(List<ClubMemberTo> members) {
        return clubMembersMessageFactory.create(members);
    }

    public EmbedCreateSpec createEmbedFromSummary(ViewEventSummaryTo summary) {
        return eventSummaryMessageFactory.create(summary);
    }

    public EmbedCreateSpec createEmbedFromUserResultSummary(String username, UserResultSummaryTo result, boolean useBadges) {
        return userOverviewMessageFactory.create(username, result, useBadges);
    }
}
