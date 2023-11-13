package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "discordbotclient", url = "https://discord.com/api", configuration = DiscordBotFeignConfig.class)
public interface DiscordGateway {

    @GetMapping("/channels/{channelId}/messages")
    List<DiscordMessageTo> getChannelMessages(@PathVariable Long channelId, @RequestParam("limit") Long limit);

    @PostMapping("/channels/{channelId}/messages")
    DiscordMessageTo createMessage(@PathVariable Long channelId, @RequestBody SimpleDiscordMessageTo message);

    @PatchMapping("/channels/{channelId}/messages/{messageId}")
    DiscordMessageTo editMessage(@PathVariable Long channelId, @PathVariable Long messageId, @RequestBody SimpleDiscordMessageTo message);


    default Optional<DiscordMessageTo> getLastChannelMessage(Long channelId) {

        List<DiscordMessageTo> channelMessages = getChannelMessages(channelId, 1L);

        if (channelMessages.isEmpty()) {
            return Optional.empty();
        } else {

            return Optional.of(channelMessages.get(channelMessages.size() - 1));
        }
    }
}
