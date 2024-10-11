package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordChannelTo(Long id, String name, @JsonProperty("parent_id") Long parentId) {
}
