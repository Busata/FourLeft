package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordActiveThreadsTo;
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

    @GetMapping("/channels/{channelId}/messages")
    List<DiscordMessageTo> getChannelMessagesAfter(@PathVariable Long channelId, @RequestParam("after") Long afterMessageId,  @RequestParam("limit") Long limit);

    @PostMapping("/channels/{channelId}/messages")
    DiscordMessageTo createMessage(@PathVariable Long channelId, @RequestBody SimpleDiscordMessageTo message);

    @PatchMapping("/channels/{channelId}/messages/{messageId}")
    DiscordMessageTo editMessage(@PathVariable Long channelId, @PathVariable Long messageId, @RequestBody SimpleDiscordMessageTo message);

    @DeleteMapping("/channels/{channelId}/messages/{messageId}")
    void deleteMessage(@PathVariable Long channelId, @PathVariable Long messageId);

    @GetMapping("/guilds/{guildId}/threads/active")
    DiscordActiveThreadsTo getThreads(@PathVariable Long guildId);

    default Optional<DiscordMessageTo> getLastChannelMessage(Long channelId) {

        List<DiscordMessageTo> channelMessages = getChannelMessages(channelId, 1L);

        if (channelMessages.isEmpty()) {
            return Optional.empty();
        } else {

            return Optional.of(channelMessages.get(channelMessages.size() - 1));
        }
    }
}
