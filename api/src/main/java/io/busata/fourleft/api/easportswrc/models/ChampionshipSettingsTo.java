package io.busata.fourleft.api.easportswrc.models;

public record ChampionshipSettingsTo(String name, Long format, Long bonusPointsMode, Long scoringSystem,
                                     Boolean isTuningAllowed, Boolean isHardcoreDamageEnabled, Long trackDegradation,
                                     Boolean isAssistsAllowed) {
}
