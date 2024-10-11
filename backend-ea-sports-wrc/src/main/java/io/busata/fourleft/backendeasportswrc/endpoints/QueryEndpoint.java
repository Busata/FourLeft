package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.SetupChannelResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordActiveThreadsTo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
class QueryEndpoint {
    private final DiscordGateway discordGateway;

    @GetMapping("/api_v2/query/setups")
    public List<SetupChannelResultTo> getSetups() {
        return discordGateway.getThreads(892050958723469332L).threads().stream().filter(channel -> {
            return channel.parentId() == 1293964993573683251L;
        }).map(channel -> {
            return new SetupChannelResultTo(channel.id(), channel.name());
        }).toList();
    }
}
