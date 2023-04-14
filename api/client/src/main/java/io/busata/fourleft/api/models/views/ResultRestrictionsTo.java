package io.busata.fourleft.api.models.views;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResultRestrictionsTo {

        UUID id;
        String vehicleClass;

        UUID resultId;
        String challengeId;
        String eventId;

        List<VehicleTo> restrictedVehicles;

        public boolean isValidVehicle(String vehicleName) {
                return this.restrictedVehicles.stream().anyMatch(vehicleTo -> vehicleTo.displayName().equalsIgnoreCase(vehicleName));
        }

}
