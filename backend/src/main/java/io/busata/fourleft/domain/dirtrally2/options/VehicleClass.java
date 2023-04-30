package io.busata.fourleft.domain.dirtrally2.options;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

import static java.util.List.of;

public enum VehicleClass {

    GROUP_A("eRallyGrpACaps", "Group A", VehicleDriveTrain.AWD, of(Vehicle.MITSUBISHI_LANCER_EVOLUTION_VI, Vehicle.SUBARU_IMPREZA_1995, Vehicle.LANCIA_DELTA_HF_INTEGRALE, Vehicle.FORD_ESCORT_RS_COSWORTH, Vehicle.SUBARU_LEGACY_RS)),
    GROUP_B_4WD("eRallyGrpB4wdCaps", "Group B (4WD)", VehicleDriveTrain.AWD, of(Vehicle.AUDI_SPORT_QUATTRO_S1_E2, Vehicle.PEUGEOT_205_T16_EVO_2, Vehicle.LANCIA_DELTA_S4, Vehicle.FORD_RS200, Vehicle.MG_METRO_6R4)),
    GROUP_B_RWD("eRallyGrpBRwdCaps", "Group B (RWD)", VehicleDriveTrain.RWD, of(Vehicle.LANCIA_037_EVO_2, Vehicle.OPEL_MANTA_400, Vehicle.BMW_M1_PROCAR_RALLY, Vehicle.PORSCHE_911_SC_RS)),
    F2_KIT_CAR("eRallyKitcarCaps", "F2 Kit Car", VehicleDriveTrain.FWD, of(Vehicle.PEUGEOT_306_MAXI, Vehicle.SEAT_IBIZA_KIT_CAR, Vehicle.VOLKSWAGEN_GOLF_KITCAR)),
    R5("eRallyR5Caps", "R5", VehicleDriveTrain.AWD, of(Vehicle.FORD_FIESTA_R5, Vehicle.FORD_FIESTA_R5_MKII, Vehicle.PEUGEOT_208_T16_R5, Vehicle.MITSUBISHI_SPACE_STAR_R5, Vehicle.SKODA_FABIA_R5, Vehicle.CITROEN_C3_R5, Vehicle.VOLKSWAGEN_POLO_GTI_R5)),
    CC_2000("eRallyUpTo20004wdCaps", "Up to 2000cc (4WD)", VehicleDriveTrain.AWD, of(Vehicle.FORD_FOCUS_RS_RALLY_2001, Vehicle.SUBARU_IMPREZA_2001, Vehicle.CITROEN_C4_RALLY, Vehicle.SKODA_FABIA_RALLY, Vehicle.FORD_FOCUS_RS_RALLY_2007, Vehicle.SUBARU_IMPREZA, Vehicle.PEUGEOT_206_RALLY, Vehicle.SUBARU_IMPREZA_S4_RALLY)),
    NR4_R4("eRallyNr4R4Caps","NR4/R4", VehicleDriveTrain.AWD, of(Vehicle.SUBARU_WRX_STI_NR4, Vehicle.MITSUBISHI_LANCER_EVOLUTION_X)),
    H2_RWD("eRallyH2RwdCaps","H2 RWD", VehicleDriveTrain.RWD, of(Vehicle.FORD_ESCORT_MK_II, Vehicle.ALPINE_RENAULT_A110_1600_S, Vehicle.FIAT_131_ABARTH_RALLY, Vehicle.OPEL_KADETT_C_GT_E)),
    H3_RWD("eRallyH3RwdCaps","H3 RWD", VehicleDriveTrain.RWD, of(Vehicle.BMW_E30_M3_EVO_RALLY, Vehicle.OPEL_ASCONA_400, Vehicle.LANCIA_STRATOS, Vehicle.RENAULT_5_TURBO, Vehicle.DATSUN_240Z, Vehicle.FORD_SIERRA_COSWORTH_RS500)),
    R2("eRallyR2Caps","R2", VehicleDriveTrain.FWD, of(Vehicle.FORD_FIESTA_R2, Vehicle.OPEL_ADAM_R2, Vehicle.PEUGEOT_208_R2)),
    H2_FWD("eRallyH2FwdCaps", "H2 FWD", VehicleDriveTrain.FWD, of(Vehicle.VOLKSWAGEN_GOLF_GTI_16V, Vehicle.PEUGEOT_205_GTI)),
    H1_FWD("eRallyH1FwdCaps","H1 FWD", VehicleDriveTrain.FWD, of(Vehicle.MINI_COOPER_S, Vehicle.LANCIA_FULVIA_HF, Vehicle.DS_21)),
    RGT("eRallyRGtCaps","Rally GT", VehicleDriveTrain.RWD, of(Vehicle.CHEVROLET_CAMARO_GT4_R, Vehicle.PORSCHE_911_RGT_RALLY_SPEC, Vehicle.ASTON_MARTIN_V8_VANTAGE_GT4, Vehicle.FORD_MUSTANG_GT4, Vehicle.BMW_M2_COMPETITION));

    @Getter
    private String id;
    private String displayName;

    private VehicleDriveTrain driveTrain;

    @Getter
    private List<Vehicle> vehicles;

    VehicleClass(String id, String displayName, VehicleDriveTrain driveTrain, List<Vehicle> vehicles) {
        this.id = id;
        this.displayName = displayName;
        this.driveTrain = driveTrain;
        this.vehicles = vehicles;
    }

    public static VehicleClass findById(String id) {
        return Arrays.stream(VehicleClass.values()).filter(x -> x.getId().equalsIgnoreCase(id)).findFirst().orElseThrow();
    }


    public String id() {
        return this.id;
    }
    public String displayName() {
        return this.displayName;
    }
}
