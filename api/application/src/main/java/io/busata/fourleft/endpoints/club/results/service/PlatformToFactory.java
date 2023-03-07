package io.busata.fourleft.endpoints.club.results.service;

import io.busata.fourleft.api.models.PlatformTo;
import io.busata.fourleft.domain.players.ControllerType;
import io.busata.fourleft.domain.players.Platform;
import io.busata.fourleft.domain.players.PlayerInfoRepository;
import io.busata.fourleft.helpers.Factory;
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
