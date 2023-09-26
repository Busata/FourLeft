package io.busata.fourleft.application.dirtrally2.aggregators;

import io.busata.fourleft.api.models.PlatformTo;
import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.Platform;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

@Factory
@RequiredArgsConstructor
public class PlatformToFactory {


    public PlatformTo createFromRacenet(PlayerInfo playerInfo) {
         return new PlatformTo(playerInfo.getPlatform(), playerInfo.getController());
    }
}
