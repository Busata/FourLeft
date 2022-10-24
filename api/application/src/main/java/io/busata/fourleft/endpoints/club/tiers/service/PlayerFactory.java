package io.busata.fourleft.endpoints.club.tiers.service;

import io.busata.fourleft.api.models.tiers.PlayerTo;
import io.busata.fourleft.domain.tiers.models.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerFactory {
    private final TierFactory tierFactory;

    public PlayerTo create(Player player) {
        return new PlayerTo(
                player.getId(),
                player.getRacenet(),
                player.getTiers().stream().map(tierFactory::create).toList()
        );
    }
}
