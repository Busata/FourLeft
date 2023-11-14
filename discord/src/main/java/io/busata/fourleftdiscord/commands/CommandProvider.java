package io.busata.fourleftdiscord.commands;

import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

public interface CommandProvider {
    void modify(ImmutableApplicationCommandRequest.Builder rootCommand);
}
