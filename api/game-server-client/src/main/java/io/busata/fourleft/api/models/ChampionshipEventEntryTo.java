package io.busata.fourleft.api.models;

public record ChampionshipEventEntryTo(String countryId,
                                       String eventName,
                                       String stageName,
                                       String challengeId,
                                       String eventId,
                                       Long stageId,
                                       String stageCondition,
                                       String vehicleClass,
                                       boolean isCurrent,
                                       boolean isFinished
) {
}
