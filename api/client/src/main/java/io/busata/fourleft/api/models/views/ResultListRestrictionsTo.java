package io.busata.fourleft.api.models.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ResultListRestrictionsTo extends ResultRestrictionsTo {
        List<VehicleTo> allowedVehicles;


        public boolean isValidVehicle(String vehicleName) {
                return this.allowedVehicles.stream().anyMatch(vehicleTo -> vehicleTo.displayName().equalsIgnoreCase(vehicleName));
        }

}
