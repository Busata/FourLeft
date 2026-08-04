package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DiscordArchivedThreadsTo(List<DiscordChannelTo> threads, @JsonProperty("has_more") boolean hasMore) {
}
