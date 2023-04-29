package io.busata.fourleft.domain.dirtrally2.clubs;

import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.Platform;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlatformInfo {
    String name;
    @Setter
    Platform platform;
    @Setter
    ControllerType controllerType;

    public PlatformInfo(String name) {
        this.name = name;
        this.platform = Platform.UNKNOWN;
        this.controllerType = ControllerType.UNKNOWN;
    }
}
