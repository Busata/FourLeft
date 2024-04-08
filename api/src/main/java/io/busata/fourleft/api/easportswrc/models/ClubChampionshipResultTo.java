package io.busata.fourleft.api.easportswrc.models;

import java.time.ZonedDateTime;
import java.util.List;

public record ClubChampionshipResultTo(String id, String string, ZonedDateTime absoluteOpenDate,
                                       ZonedDateTime absoluteCloseDate,
                                       ChampionshipSettingsTo championshipSettingsTo,
                                       List<ClubEventResultTo> events) {
}
