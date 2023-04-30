package io.busata.fourleft.api.models.configuration;



import io.busata.fourleft.api.models.StandingPointPairTo;

import java.util.List;
import java.util.UUID;

public record PointSystemTo(
    UUID id,
    String description,

    int defaultStandingPoint,
    int defaultPowerstagePoint,
    int defaultDNFPoint,

    List<StandingPointPairTo> standingPoints,
    List<StandingPointPairTo> powerStagePoints

) {
}
