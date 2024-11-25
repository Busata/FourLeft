package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementEndpoint {
    private final DiscordGateway discordGateway;

    @PostConstruct
    public void setup() {
       //this.discordGateway.deleteMessage(1173281586184204348L, 1310613982247125032L);
    }
}
