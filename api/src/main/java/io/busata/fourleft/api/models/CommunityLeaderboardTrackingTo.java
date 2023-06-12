package io.busata.fourleft.api.models;

import java.util.UUID;

public record CommunityLeaderboardTrackingTo(
        UUID id,

        String racenet,
        String alias,

        boolean trackRallyCross,
        boolean trackDaily,
        boolean trackMonthly,
        boolean trackWeekly

) {
}
