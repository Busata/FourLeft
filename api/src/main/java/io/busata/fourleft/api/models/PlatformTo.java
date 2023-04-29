package io.busata.fourleft.api.models;


import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.Platform;

public record PlatformTo(
        Platform platform,
        ControllerType controller
) {
}
