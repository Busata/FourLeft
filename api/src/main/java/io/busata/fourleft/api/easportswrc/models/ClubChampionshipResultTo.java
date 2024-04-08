package io.busata.fourleft.api.easportswrc.models;

public record ClubChampionshipResultTo(String id, String string, java.time.ZonedDateTime absoluteOpenDate,
                                       java.time.ZonedDateTime absoluteCloseDate,
                                       ChampionshipSettingsTo championshipSettingsTo,
                                       java.util.List<ClubEventResultTo> events) {
}
