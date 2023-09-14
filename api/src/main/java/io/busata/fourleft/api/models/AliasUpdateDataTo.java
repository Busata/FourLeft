package io.busata.fourleft.api.models;

import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.Platform;

import java.util.List;
import java.util.UUID;

public record AliasUpdateDataTo (
        UUID id,
        String displayName,
        ControllerType controller,
        Platform platform,
        String racenet,
        List<String> aliases

) {
}
