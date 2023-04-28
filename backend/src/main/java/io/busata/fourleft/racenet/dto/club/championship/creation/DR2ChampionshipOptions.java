package io.busata.fourleft.racenet.dto.club.championship.creation;

import java.util.List;

public record DR2ChampionshipOptions(
        List<DR2LocationOption> locations,
        List<DR2VehicleOption> vehicleOptions
) {
}
