package io.busata.fourleft.racenet.dto.communityevents;


import io.busata.fourleft.domain.clubs.models.DR2CommunityEventType;

import java.util.List;

public record DR2CommunityEvent(
        DR2CommunityEventType type,
        String name,
        List<DR2ChallengeGroup> challengeGroups
) {
}
