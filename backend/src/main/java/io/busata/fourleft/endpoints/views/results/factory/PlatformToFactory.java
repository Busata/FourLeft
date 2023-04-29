package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.PlatformTo;
import io.busata.fourleft.api.models.ControllerType;
import io.busata.fourleft.api.models.Platform;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

@Factory
@RequiredArgsConstructor
public class PlatformToFactory {

    private final PlayerInfoRepository playerInfoRepository;

    public PlatformTo createFromRacenet(String racenet) {
        return playerInfoRepository.findByRacenet(racenet).map(playerInfo -> {
            return new PlatformTo(playerInfo.getPlatform(), playerInfo.getController());
        }).orElse(new PlatformTo(Platform.UNKNOWN, ControllerType.UNKNOWN));
    }
}
