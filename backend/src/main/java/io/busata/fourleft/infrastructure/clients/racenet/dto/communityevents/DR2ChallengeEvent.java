package io.busata.fourleft.infrastructure.clients.racenet.dto.communityevents;

import java.util.List;

public record DR2ChallengeEvent(
        String id,
        String name,
        String discipline,
        List<DR2ChallengeStage> stages
) {
}
