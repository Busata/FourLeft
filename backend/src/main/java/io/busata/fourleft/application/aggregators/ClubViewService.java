package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.application.discord.DiscordChannelConfigurationToFactory;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubViewService {
    private final ClubViewRepository clubViewRepository;

    private final DiscordChannelConfigurationToFactory discordChannelConfigurationToFactory;

    public List<ClubViewTo> getClubViews() {
        return clubViewRepository.findAll().stream().map(discordChannelConfigurationToFactory::createClubViewTo).toList();
    }


}
