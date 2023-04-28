package io.busata.fourleft.endpoints.discord.integration;

import lombok.Getter;

public enum DisordChannelType {
    TEXT(0);

    @Getter private final int type;

    DisordChannelType(int type) {
        this.type = type;
    }
}
