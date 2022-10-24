package io.busata.fourleft.api.models;

import java.util.List;

public record ChampionshipStageEntryTo(String country, String vehicleClass, List<String> stageNames) {
}
