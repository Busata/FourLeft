package io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ControllerFilter {
    ALL("Unspecified"),
    CONTROLLER("Off"),
    WHEEL("On");

    @Getter()
    private final String value;
}
