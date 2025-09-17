package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementEndpoint {
    private final DiscordGateway discordGateway;

    @PostConstruct
    public void setup() {
       //this.discordGateway.deleteMessage(1173281586184204348L, 1310613982247125032L);
    }

    private final ApplicationEventPublisher eventPublisher;


    @PostMapping("/api_v2/management/test_30129")
    public void testClubEvent() {
        this.eventPublisher.publishEvent(new ClubEventEnded("30129"));
    }
}
