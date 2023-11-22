package io.busata.fourleft.backendeasportswrc.application.discord.messages;


import io.busata.fourleft.api.easportswrc.events.ClubChampionshipStarted;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResultsService;
import io.busata.fourleft.backendeasportswrc.domain.services.championships.ChampionshipService;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChampionshipStartedMessageService {

    private final DiscordGateway discordGateway;

    private final DiscordClubConfigurationService discordClubConfigurationService;
    private final ClubResultsService clubResultsService;
    private final ClubService clubService;
    private final ClubEventsMessageFactory clubEventsMessageFactory;
    private final ChampionshipService championshipService;

    private final ClubResultsMessageFactory clubResultsMessageFactory;

    @EventListener
    public void handleClubEvent(ClubChampionshipStarted championshipStarted) {
        discordClubConfigurationService.findByClubId(championshipStarted.clubId()).forEach(configuration -> {
            List<MessageEmbed> embeds = new ArrayList<>();

            clubService.getActiveChampionshipId(championshipStarted.clubId()).flatMap(championshipService::findChampionship).ifPresent(results -> {
                MessageEmbed resultPost = clubEventsMessageFactory.createEventSummary(results);
                embeds.add(resultPost);
            });

            clubResultsService.getCurrentResults(championshipStarted.clubId()).ifPresent(results -> {
                MessageEmbed resultPost = clubResultsMessageFactory.createResultPost(results, configuration);
                embeds.add(resultPost);
            });

            embeds.forEach(embed -> {
                discordGateway.createMessage(configuration.getChannelId(), new SimpleDiscordMessageTo(null, List.of(embed.toData().toString())));
            });
        });
    }


}
