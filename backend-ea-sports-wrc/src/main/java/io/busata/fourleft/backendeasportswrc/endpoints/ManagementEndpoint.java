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
//        this.discordGateway.deleteMessage(1173281341073264745L, 1186273579348791418L);
//        this.discordGateway.deleteMessage(1173281341073264745L, 1186273589301887007L);
    }
}
