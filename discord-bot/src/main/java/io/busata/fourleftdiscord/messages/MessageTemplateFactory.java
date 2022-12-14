package io.busata.fourleftdiscord.messages;

import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableView;
import io.busata.fourleftdiscord.messages.creation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageTemplateFactory {
    private final ChampionshipStandingsMessageFactory championshipStandingsMessageFactory;
    private final ClubEventResultMessageFactory clubEventResultMessageFactory;
    private final ClubMembersMessageFactory clubMembersMessageFactory;
    private final UserOverviewMessageFactory userOverviewMessageFactory;
    private final ChampionshipSummaryMessageFactory championshipSummaryMessageFactory;
    private final CommunityEventMessageFactory communityEventMessageFactory;

    private final AutoPostResultMessageFactory autoPostResultMessageFactory;

    public String createAutopostMessage(AutoPostableView view) {
        return autoPostResultMessageFactory.createAutopostMessage(view);
    }

    public List<EmbedCreateSpec> createEmbedFromClubResult(ViewResultTo result) {
        return clubEventResultMessageFactory.createDefault(result);
    }

    public EmbedCreateSpec createEmbedFromStandingEntries(ViewPointsTo result) {
        return championshipStandingsMessageFactory.create(result);
    }
    public List<EmbedCreateSpec> createPowerstageEmbed(ViewResultTo clubResult) {
        return clubEventResultMessageFactory.createPowerStage(clubResult);
    }

    public List<EmbedCreateSpec> createEmbedFromCommunityEventResults(List<CommunityChallengeSummaryTo> events) {
        return communityEventMessageFactory.getEmbeds(events);
    }

    public EmbedCreateSpec createEmbedFromMembers(List<ClubMemberTo> members) {
        return clubMembersMessageFactory.create(members);
    }

    public EmbedCreateSpec createEmbedFromSummary(ChampionshipEventSummaryTo summary) {
        return championshipSummaryMessageFactory.create(summary);
    }

    public EmbedCreateSpec createEmbedFromUserResultSummary(UserResultSummaryTo result, boolean useBadges) {
        return userOverviewMessageFactory.create(result, useBadges);
    }
}
