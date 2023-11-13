package io.busata.fourleft.api.easportswrc.models;

import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.PeripheralType;
import io.busata.fourleft.common.Platform;

public record ProfileTo(
        String id,
        String displayName,
        ControllerType controller,
        Platform platform,
        PeripheralType peripheral,
        String racenet,
        boolean trackDiscord) {
}
