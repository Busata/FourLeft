package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models;


import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

public record SimpleDiscordMessageTo(String content,
                                     @JsonRawValue List<String> embeds) {
}
