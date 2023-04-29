package io.busata.fourleft.api.models;



import java.util.List;

public record CommunityChallengeSummaryTo(
        DR2CommunityEventType type,
        String vehicleClass,
        List<String> eventLocations,
        String firstStageName,

        CommunityChallengeBoardEntryTo topOnePercentEntry,
        CommunityChallengeBoardEntryTo firstEntry,
        List<CommunityChallengeBoardEntryTo> entries
) {
}
