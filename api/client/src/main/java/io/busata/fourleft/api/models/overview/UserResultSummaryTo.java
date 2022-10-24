package io.busata.fourleft.api.models.overview;

import java.util.List;

public record UserResultSummaryTo(
        List<CommunityResultSummaryTo> communityActiveEventSummaries,
        List<CommunityResultSummaryTo> communityPreviousEventSummaries,
        List<ClubResultSummaryTo> clubActiveEventSummaries,
        List<ClubResultSummaryTo> clubPreviousEventSummaries

) {
}
