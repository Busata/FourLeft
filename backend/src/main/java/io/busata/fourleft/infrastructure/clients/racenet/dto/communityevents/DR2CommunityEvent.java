package io.busata.fourleft.infrastructure.clients.racenet.dto.communityevents;


import io.busata.fourleft.common.DR2CommunityEventType;

import java.util.List;

public record DR2CommunityEvent(
        DR2CommunityEventType type,
        String name,
        List<DR2ChallengeGroup> challengeGroups
) {
}
