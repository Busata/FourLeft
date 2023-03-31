package io.busata.fourleft.api.models.configuration;

import java.util.List;
import java.util.UUID;

public record DiscordChannelConfigurationTo(
        UUID id,
        Long channelId,
        String description,
        List<ClubViewTo> commandClubViews,
        List<ClubViewTo> autopostClubViews
) {

    public boolean hasAutopostViews() {
        return !this.autopostClubViews.isEmpty();
    }

}
