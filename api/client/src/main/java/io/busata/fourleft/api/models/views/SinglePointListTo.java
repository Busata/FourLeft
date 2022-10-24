package io.busata.fourleft.api.models.views;

import java.util.List;

public record SinglePointListTo(
        String name,
        List<PointPairTo> points
) {
}
