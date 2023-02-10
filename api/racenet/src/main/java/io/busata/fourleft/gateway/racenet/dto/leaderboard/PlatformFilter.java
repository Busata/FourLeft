package io.busata.fourleft.gateway.racenet.dto.leaderboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlatformFilter {
    NONE("None"),
    STEAM("Steam"),
    PLAYSTATION("PlaystationNetwork"),
    XBOX("MicrosoftLive");

    @Getter
    private final String filterValue;
}
