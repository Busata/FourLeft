package io.busata.fourleft.racenet.dto.club;

import java.util.List;

public record DR2ClubResultChampionshipEvent(
        String id,
        String challengeId,
        String name,
        List<DR2ClubResultChampionshipEventStage> stages
) {
}
