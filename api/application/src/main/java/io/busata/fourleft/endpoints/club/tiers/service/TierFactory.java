package io.busata.fourleft.endpoints.club.tiers.service;

import io.busata.fourleft.api.models.tiers.TierActiveInfoTo;
import io.busata.fourleft.api.models.tiers.TierTo;
import io.busata.fourleft.api.models.tiers.VehicleTo;
import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.domain.options.models.VehicleClass;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.domain.tiers.models.Tier;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TierFactory {

    public TierActiveInfoTo create(Event event) {
        VehicleClass vehicleClass = VehicleClass.findById(event.getVehicleClass());
        return new TierActiveInfoTo(
                event.getReferenceId(),
                event.getChallengeId(),
                event.getCountry(),
                event.getStages().stream().map(Stage::getName).collect(Collectors.toList()),
                vehicleClass.displayName(),
                vehicleClass.getVehicles().stream().map(this::create).toList()
        );
    }

    public VehicleTo create(Vehicle vehicle) {
        return new VehicleTo(
                vehicle.name(),
                vehicle.getDisplayName()
        );
    }

    public TierTo create(Tier tier) {
        return new TierTo(tier.getId(), tier.getName(), tier.getClubId());
    }
}
