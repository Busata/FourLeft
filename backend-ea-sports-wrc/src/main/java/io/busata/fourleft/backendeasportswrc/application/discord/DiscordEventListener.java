package io.busata.fourleft.backendeasportswrc.application.discord;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.DiscordAutoPostingService;
import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordEventListener {

    private final DiscordAutoPostingService discordAutoPostingService;


    @EventListener
    public void handleLeaderboardUpdate(LeaderboardUpdatedEvent event) {
        //Handle autoposting
        log.info("Leaderboard for club {} updated.", event.clubId());
        this.discordAutoPostingService.syncResults(event.clubId());
    }

    @EventListener
    public void handleEventEnded(ClubEventEnded event) {
        //Handle event end
        log.info("Event for club {} ended.", event.clubId());
    }

}
