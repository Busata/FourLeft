package io.busata.fourleft.api.models.configuration.create;

import io.busata.fourleft.api.models.configuration.ClubViewTo;

import java.util.UUID;

public record DiscordChannelConfigurationTo(
        UUID id,
        boolean enableAutoposts,

        Long channelId,

        ClubViewTo clubView
) {

    public boolean includesClub(Long clubId) {
        return clubView.resultsView().getAssociatedClubs().contains(clubId);
    }


}
