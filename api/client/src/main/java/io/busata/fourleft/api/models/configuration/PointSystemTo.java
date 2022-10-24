package io.busata.fourleft.api.models.configuration;

import io.busata.fourleft.domain.configuration.points.PointPair;

import java.util.List;
import java.util.UUID;

public record PointSystemTo(
    UUID id,
    String description,
    List<PointPair> powerStagePoints,
    List<PointPair> rankingPoints
) {
}
