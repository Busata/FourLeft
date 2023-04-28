package io.busata.fourleft.domain.options.models;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public enum VehicleClassGroups {

    UNPOPULAR(
            VehicleClass.H1_FWD,
            VehicleClass.H2_FWD,
            VehicleClass.F2_KIT_CAR
    ),
    CLASSIC(
            VehicleClass.H2_RWD,
            VehicleClass.H3_RWD
    ),
    MODERN_ALTERNATIVE(
            VehicleClass.R2,
            VehicleClass.RGT
    ),
    MODERN(
            VehicleClass.R5,
            VehicleClass.CC_2000
            ),
    MODERN_CLASSICS(
            VehicleClass.GROUP_A,
            VehicleClass.NR4_R4
    ),
    GROUP_B(
            VehicleClass.GROUP_B_4WD,
            VehicleClass.GROUP_B_RWD
    );

    @Getter
    private final List<VehicleClass> vehicleClasses;

    VehicleClassGroups(VehicleClass... vehicleClasses) {
        this.vehicleClasses = Arrays.asList(vehicleClasses);
    }

    public boolean containsVehicleOption(VehicleClass option) {
        return this.vehicleClasses.contains(option);
    }

    public static VehicleClassGroups findGroup(VehicleClass option) {
        return Arrays.stream(VehicleClassGroups.values()).filter(vehicleOptions -> vehicleOptions.containsVehicleOption(option)).findFirst().orElseThrow();
    }

}
