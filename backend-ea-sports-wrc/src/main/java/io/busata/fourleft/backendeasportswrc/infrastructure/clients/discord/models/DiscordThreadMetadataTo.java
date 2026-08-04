package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordThreadMetadataTo(@JsonProperty("archive_timestamp") String archiveTimestamp) {
}
