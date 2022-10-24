package io.busata.fourleft.gateway.racenet.dto.club.championship.creation;

import java.util.List;

public record DR2LocationOption(
        String id,
        String name,
        boolean isDlc,
        List<DR2RouteOption> routes,
        List<DR2ConditionOption> supportedConditions
) {
}
