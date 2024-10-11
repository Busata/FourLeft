package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models;

import java.util.List;

public record DiscordActiveThreadsTo(List<DiscordChannelTo> threads) {
}
