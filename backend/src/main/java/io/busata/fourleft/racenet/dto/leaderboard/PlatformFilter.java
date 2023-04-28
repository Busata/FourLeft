package io.busata.fourleft.racenet.dto.leaderboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlatformFilter {
    NONE("None"),
    STEAM("Steam"),
    PLAYSTATION("PlaystationNetwork"),
    XBOX("MicrosoftLive");

    @Getter()
    private final String value;
}
