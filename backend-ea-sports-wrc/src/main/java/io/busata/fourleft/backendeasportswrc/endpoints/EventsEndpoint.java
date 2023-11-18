package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.messages.ClubEventsMessageFactory;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.championships.ChampionshipService;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventsEndpoint {
    private final DiscordClubConfigurationService discordClubConfigurationService;

    private final ClubService clubService;
    private final ClubEventsMessageFactory clubEventsMessageFactory;
    private final ChampionshipService championshipService;

    @GetMapping("/api_v2/events/{channelId}/summary")
    String getEventSummary(@PathVariable Long channelId) {
        DiscordClubConfiguration discordClubConfiguration = discordClubConfigurationService.findByChannelId(channelId).orElseThrow();

        return clubService.getActiveChampionshipId(discordClubConfiguration.getClubId())
                .or(() -> clubService.getUpcomingChampionshipId(discordClubConfiguration.getClubId()))
                .flatMap(championshipService::findChampionship)
                .map(clubEventsMessageFactory::createEventSummary)
                .map(MessageEmbed::toData)
                .map(DataObject::toString)
                .orElse("");
    }
}
