package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordUserTo(String id, String username, @JsonProperty("global_name") String globalName, String avatar) {

}
