package io.busata.fourleft.api.models.configuration.create;

import io.busata.fourleft.api.models.configuration.ClubViewTo;

public record CreateDiscordChannelConfigurationTo(
        boolean enableAutoposts,
        ClubViewTo clubView
) {

}
