package io.busata.fourleft.gateway.racenet.dto.communityevents;

import java.util.List;

public record DR2ChallengeGroup(
        String name,
        List<DR2Challenge> challenges
) {
}
