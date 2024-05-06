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
        var messages = this.discordGateway.getChannelMessagesAfter(1218167622131257394L, 1236342471374667796L, 100L);

        messages.forEach(message -> {
            try {

                if (message.author().id().equals("961645445250171001")) {
                    this.discordGateway.deleteMessage(1218167622131257394L, message.id());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // this.discordGateway.deleteMessage(1173281341073264745L,
        // 1186273589301887007L);
    }
}
