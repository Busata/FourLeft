package io.busata.fourleft.domain.dirtrally2.options;

import lombok.Getter;

public enum Vehicle {
    MINI_COOPER_S("MINI Cooper S"),
    BMW_M1_PROCAR_RALLY("BMW M1 Procar Rally"),
    RENAULT_5_TURBO("Renault 5 Turbo"),
    VOLKSWAGEN_POLO_GTI_R5("Volkswagen Polo GTI R5"),
    FORD_RS200("Ford RS200"),
    MG_METRO_6R4("MG Metro 6R4"),
    SUBARU_WRX_STI_NR4("SUBARU WRX STI NR4"),
    PEUGEOT_208_T16_R5("Peugeot 208 T16 R5"),
    SEAT_IBIZA_KIT_CAR("Seat Ibiza Kit Car"),
    MITSUBISHI_SPACE_STAR_R5("Mitsubishi Space Star R5"),
    LANCIA_FULVIA_HF("Lancia Fulvia HF"),
    SKODA_FABIA_RALLY("ŠKODA Fabia Rally"),
    PEUGEOT_306_MAXI("Peugeot 306 Maxi"),
    FORD_FOCUS_RS_RALLY_2001("Ford Focus RS Rally 2001"),
    CHEVROLET_CAMARO_GT4_R("Chevrolet Camaro GT4.R"),
    FORD_FOCUS_RS_RALLY_2007("Ford Focus RS Rally 2007"),
    SUBARU_IMPREZA_S4_RALLY("SUBARU Impreza S4 Rally"),
    FORD_MUSTANG_GT4("Ford Mustang GT4"),
    AUDI_SPORT_QUATTRO_S1_E2("Audi Sport quattro S1 E2"),
    ASTON_MARTIN_V8_VANTAGE_GT4("Aston Martin V8 Vantage GT4"),
    PEUGEOT_205_GTI("Peugeot 205 GTI"),
    CITROEN_C4_RALLY("Citroën C4 Rally"),
    VOLKSWAGEN_GOLF_GTI_16V("Volkswagen Golf GTI 16V"),
    OPEL_ADAM_R2("Opel Adam R2"),
    FORD_ESCORT_RS_COSWORTH("Ford Escort RS Cosworth"),
    FORD_FIESTA_R5("Ford Fiesta R5"),
    SUBARU_IMPREZA_2001("SUBARU Impreza (2001)"),
    BMW_M2_COMPETITION("BMW M2 Competition"),
    MITSUBISHI_LANCER_EVOLUTION_VI("Mitsubishi Lancer Evolution VI"),
    PORSCHE_911_SC_RS("Porsche 911 SC RS"),
    FORD_FIESTA_R5_MKII("Ford Fiesta R5 MKII"),
    SUBARU_IMPREZA_1995("SUBARU Impreza 1995"),
    DATSUN_240Z("Datsun 240Z"),
    BMW_E30_M3_EVO_RALLY("BMW E30 M3 Evo Rally"),
    PORSCHE_911_RGT_RALLY_SPEC("Porsche 911 RGT Rally Spec"),
    ALPINE_RENAULT_A110_1600_S("Alpine Renault A110 1600 S"),
    FORD_ESCORT_MK_II("Ford Escort Mk II"),
    OPEL_ASCONA_400("Opel Ascona 400"),
    DS_21("DS 21"),
    CITROEN_C3_R5("Citroën C3 R5"),
    PEUGEOT_208_R2("Peugeot 208 R2"),
    FIAT_131_ABARTH_RALLY("Fiat 131 Abarth Rally"),
    SUBARU_IMPREZA("SUBARU Impreza"),
    VOLKSWAGEN_GOLF_KITCAR("Volkswagen Golf Kitcar"),
    PEUGEOT_206_RALLY("Peugeot 206 Rally"),
    FORD_SIERRA_COSWORTH_RS500("Ford Sierra Cosworth RS500"),
    LANCIA_DELTA_S4("Lancia Delta S4"),
    OPEL_KADETT_C_GT_E("Opel Kadett C GT/E"),
    LANCIA_STRATOS("Lancia Stratos"),
    LANCIA_DELTA_HF_INTEGRALE("Lancia Delta HF Integrale"),
    MITSUBISHI_LANCER_EVOLUTION_X("Mitsubishi Lancer Evolution X"),
    OPEL_MANTA_400("Opel Manta 400"),
    LANCIA_037_EVO_2("Lancia 037 Evo 2"),
    SKODA_FABIA_R5("ŠKODA Fabia R5"),
    PEUGEOT_205_T16_EVO_2("Peugeot 205 T16 Evo 2"),
    FORD_FIESTA_R2("Ford Fiesta R2"),
    SUBARU_LEGACY_RS("SUBARU Legacy RS");

    @Getter
    String displayName;
    Vehicle(String displayName) {
        this.displayName = displayName;
    }

    public boolean matches(String name) {
        return this.displayName.equalsIgnoreCase(name);
    }
}
    /* RX cars if ever needed
    OPEL_CORSA_SUPER_1600("Opel Corsa Super 1600"),
    FORD_FIESTA_OMSE_SUPERCAR_LITES("Ford Fiesta OMSE SuperCar Lites"),
    FORD_FIESTA_RALLYCROSS_MK7("Ford Fiesta Rallycross (MK7)"),
    FORD_FIESTA_RALLYCROSS_MK8("Ford Fiesta Rallycross (MK8)"),
    RENAULT_MEGANE_RS_RX("Renault Megane RS RX"),
    RENAULT_CLIO_R_S_S1600("Renault Clio R.S. S1600"),
    VOLKSWAGEN_POLO_R_SUPERCAR("Volkswagen Polo R Supercar"),
    AUDI_S1_EKS_RX_QUATTRO("Audi S1 EKS RX quattro"),
    SUBARU_WRX_STI_RALLYCROSS("SUBARU WRX STI Rallycross"),
    VOLKSWAGEN_POLO_S1600("Volkswagen Polo S1600"),
    PEUGEOT_208_WRX("Peugeot 208 WRX"),
    SPEEDCAR_XTREM("Speedcar Xtrem"),

 */