package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.SetupChannelResultTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordArchivedThreadsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordChannelTo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
class QueryEndpoint {
    private static final long GUILD_ID = 892050958723469332L;
    private static final long SETUP_FORUM_CHANNEL_ID = 1293964993573683251L;
    private static final long ARCHIVED_PAGE_SIZE = 100L;

    private final DiscordGateway discordGateway;

    @GetMapping("/api_v2/query/setups")
    public List<SetupChannelResultTo> getSetups() {
        List<DiscordChannelTo> threads = new ArrayList<>(discordGateway.getThreads(GUILD_ID).threads().stream().filter(channel -> {
            return channel.parentId() == SETUP_FORUM_CHANNEL_ID;
        }).toList());
        threads.addAll(getArchivedThreads());

        return threads.stream()
                .map(channel -> new SetupChannelResultTo(channel.id(), channel.name()))
                .toList();
    }

    private List<DiscordChannelTo> getArchivedThreads() {
        List<DiscordChannelTo> archived = new ArrayList<>();
        String before = null;
        while (true) {
            DiscordArchivedThreadsTo page = discordGateway.getPublicArchivedThreads(SETUP_FORUM_CHANNEL_ID, before, ARCHIVED_PAGE_SIZE);
            archived.addAll(page.threads());
            if (!page.hasMore() || page.threads().isEmpty()) {
                return archived;
            }
            before = page.threads().get(page.threads().size() - 1).threadMetadata().archiveTimestamp();
        }
    }
}
