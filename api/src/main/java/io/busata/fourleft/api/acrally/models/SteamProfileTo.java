package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;

public record SteamProfileTo(
        String personaName,
        String avatarUrl,
        String profileUrl,
        LocalDateTime accountCreated,
        Integer visibilityState,
        boolean vacBanned,
        int gameBanCount,
        boolean communityBanned) {
}
