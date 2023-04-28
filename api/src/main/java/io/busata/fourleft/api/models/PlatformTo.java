package io.busata.fourleft.api.models;

import io.busata.fourleft.domain.players.ControllerType;
import io.busata.fourleft.domain.players.Platform;

public record PlatformTo(
        Platform platform,
        ControllerType controller
) {
}
