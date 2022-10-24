package io.busata.fourleft.domain.options.models;

import java.util.List;

public enum StageConfiguration {

    ARGENTINA_A_FWD(StageOption.LAS_JUNTAS, StageOption.CAMINO_DE_ACANTILADOS_Y_ROCAS, StageOption.LA_MERCED),
    ARGENTINA_A_REV(StageOption.CAMINO_A_LA_PUERTA, StageOption.EL_RODEO, StageOption.CAMINO_DE_ACANTILADOS_Y_ROCAS_INVERSO),
    ARGENTINA_B_FWD(StageOption.VALLE_DE_LOS_PUENTES, StageOption.MIRAFLORES, StageOption.CAMINO_A_CONETA),
    ARGENTINA_B_REV(StageOption.VALLE_DE_LOS_PUENTES_A_LA_INVERSA, StageOption.SAN_ISIDRO, StageOption.HUILLAPRIMA),

    AUSTRALIA_A_FWD(StageOption.MOUNT_KAYE_PASS, StageOption.ROCKTON_PLAINS, StageOption.YAMBULLA_MOUNTAIN_ASCENT),
    AUSTRALIA_A_REV(StageOption.MOUNT_KAYE_PASS_REVERSE, StageOption.YAMBULLA_MOUNTAIN_DESCENT, StageOption.ROCKTON_PLAINS_REVERSE),
    AUSTRALIA_B_FWD(StageOption.CHANDLERS_CREEK, StageOption.NOORINBEE_RIDGE_ASCENT, StageOption.BONDI_FOREST),
    AUSTRALIA_B_REV(StageOption.CHANDLERS_CREEK_REVERSE, StageOption.TAYLOR_FARM_SPRINT, StageOption.NOORINBEE_RIDGE_DESCENT),

    FINLAND_A_FWD(StageOption.KONTINJARVI, StageOption.KAILAJARVI, StageOption.NAARAJARVI),
    FINLAND_A_REV(StageOption.HAMELAHTI, StageOption.JYRKYSJARVI, StageOption.PASKURI),
    FINLAND_B_FWD(StageOption.KAKARISTO, StageOption.ISO_OKSJARVI, StageOption.KOTAJARVI),
    FINLAND_B_REV(StageOption.PITKAJARVI, StageOption.JARVENKYLA, StageOption.OKSALA),

    GERMANY_A_FWD(StageOption.OBERSTEIN, StageOption.WALDAUFSTIEG, StageOption.KREUZUNGSRING),
    GERMANY_A_REV(StageOption.FRAUENBERG, StageOption.KREUZUNGSRING_REVERSE, StageOption.WALDABSTIEG),
    GERMANY_B_FWD(StageOption.HAMMERSTEIN, StageOption.VERBUNDSRING, StageOption.INNERER_FELD_SPRINT_UMGEKEHRT),
    GERMANY_B_REV(StageOption.RUSCHBERG, StageOption.INNERER_FELD_SPRINT, StageOption.VERBUNDSRING_REVERSE),

    GREECE_A_FWD(StageOption.ANODOU_FARMAKAS, StageOption.POMONA_EKRIXI, StageOption.KORYFI_DAFNI),
    GREECE_A_REV(StageOption.KATHODO_LEONTIOU, StageOption.FOURKETA_KOURVA, StageOption.AMPELONAS_ORMI),
    GREECE_B_FWD(StageOption.PERASMA_PLATANI, StageOption.OUREA_SPEVSI, StageOption.ABIES_KOILADA),
    GREECE_B_REV(StageOption.TSIRISTRA_THEA, StageOption.PEDINES_EPIDAXI, StageOption.YPSONA_TOU_DASOS),

    MONTE_CARLO_A_FWD(StageOption.PRA_DALART, StageOption.GORDOLON_COURTE_MONTEE, StageOption.COL_DE_TURINI_SPRINT_EN_MONTEE),
    MONTE_CARLO_A_REV(StageOption.COL_DE_TURINI_DEPART, StageOption.COL_DE_TURINI_SPRINT_EN_DESCENTE, StageOption.COL_DE_TURINI_DESCENTE),
    MONTE_CARLO_B_FWD(StageOption.VALLEE_DESCENDANTE, StageOption.COL_DE_TURINI_DEPART_EN_DESCENTE, StageOption.ROUTE_DE_TURINI_DESCENTE),
    MONTE_CARLO_B_REV(StageOption.ROUTE_DE_TURINI, StageOption.APPROCHE_DU_COL_DE_TURINI_MONTEE, StageOption.ROUTE_DE_TURINI_MONTEE),

    POLAND_A_FWD(StageOption.ZAROBKA, StageOption.KOPINA, StageOption.BORYSIK),
    POLAND_A_REV(StageOption.ZAGORZE, StageOption.MARYNKA, StageOption.JOZEFIN),
    POLAND_B_FWD(StageOption.JEZIORO_ROTCZE, StageOption.CZARNY_LAS, StageOption.JAGODNO),
    POLAND_B_REV(StageOption.ZIENKI, StageOption.LEJNO, StageOption.JEZIORO_LUKIE),

    SCOTLAND_A_FWD(StageOption.SOUTH_MORNINGSIDE, StageOption.OLD_BUTTERSTONE_MUIR, StageOption.ROSEBANK_FARM_REVERSE),
    SCOTLAND_A_REV(StageOption.SOUTH_MORNINGSIDE_REVERSE, StageOption.ROSEBANK_FARM, StageOption.OLD_BUTTERSTONE_MUIR_REVERSE),
    SCOTLAND_B_FWD(StageOption.NEWHOUSE_BRIDGE, StageOption.GLENCASTLE_FARM, StageOption.ANNBANK_STATION_REVERSE),
    SCOTLAND_B_REV(StageOption.NEWHOUSE_BRIDGE_REVERSE, StageOption.ANNBANK_STATION, StageOption.GLENCASTLE_FARM_REVERSE),

    SPAIN_A_FWD(StageOption.COMIENZO_DE_BELLRIU, StageOption.ASCENSO_POR_VALLE_EL_GUALET, StageOption.ASCENSO_BOSQUE_MONTVERD),
    SPAIN_A_REV(StageOption.FINAL_DE_BELLRIU, StageOption.VINEDOS_DENTRO_DEL_VALLE_PARRA, StageOption.SALIDA_DESDE_MONTVERD),
    SPAIN_B_FWD(StageOption.CENTENERA, StageOption.DESCENSO_POR_CARRETERA, StageOption.VINEDOS_DARDENYA_INVERSA),
    SPAIN_B_REV(StageOption.CAMINO_A_CENTENERA, StageOption.VINEDOS_DARDENYA, StageOption.SUBIDA_POR_CARRETERA),

    SWEDEN_A_FWD(StageOption.RANSBYSATER, StageOption.ALGSJON_SPRINT, StageOption.STOR_JANGEN_SPRINT),
    SWEDEN_A_REV(StageOption.NORRASKOGA, StageOption.STOR_JANGEN_SPRINT_REVERSE, StageOption.SKOGSRALLYT),
    SWEDEN_B_FWD(StageOption.HAMRA, StageOption.ELGSJON, StageOption.OSTRA_HINNSJON),
    SWEDEN_B_REV(StageOption.LYSVIK, StageOption.BJORKLANGEN, StageOption.ALGSJON),

    USA_A_FWD(StageOption.NORTH_FORK_PASS, StageOption.HANCOCK_CREEK_BURST, StageOption.FULLER_MOUNTAIN_ASCENT),
    USA_A_REV(StageOption.NORTH_FORK_PASS_REVERSE, StageOption.FULLER_MOUNTAIN_DESCENT, StageOption.FURY_LAKE_DEPART),
    USA_B_FWD(StageOption.BEAVER_CREEK_TRAIL_FORWARD, StageOption.HANCOCK_HILL_SPRINT_FORWARD, StageOption.TOLT_VALLEY_SPRINT_FORWARD),
    USA_B_REV(StageOption.BEAVER_CREEK_TRAIL_REVERSE, StageOption.TOLT_VALLEY_SPRINT_REVERSE, StageOption.HANCOCK_HILL_SPRINT_REVERSE),

    NEW_ZEALAND_A_FWD(StageOption.TE_AWANGA_FORWARD, StageOption.TE_AWANGA_SPRINT_FORWARD, StageOption.OCEAN_BEACH_SPRINT_REVERSE),
    NEW_ZEALAND_A_REV(StageOption.OCEAN_BEACH, StageOption.OCEAN_BEACH_SPRINT_FORWARD, StageOption.TE_AWANGA_SPRINT_REVERSE),
    NEW_ZEALAND_B_FWD(StageOption.WAIMARAMA_POINT_REVERSE, StageOption.WAIMARAMA_SPRINT_FORWARD, StageOption.ELSTHORPE_SPRINT_REVERSE),
    NEW_ZEALAND_B_REV(StageOption.WAIMARAMA_POINT_FORWARD, StageOption.ELSTHORPE_SPRINT_FORWARD, StageOption.WAIMARAMA_SPRINT_REVERSE),

    WALES_A_FWD(StageOption.RIVER_SEVERN_VALLEY, StageOption.FFERM_WYNT, StageOption.DYFFRYN_AFON),
    WALES_A_REV(StageOption.BRONFELEN, StageOption.DYFFRYN_AFON_REVERSE, StageOption.FFERM_WYNT_REVERSE),
    WALES_B_FWD(StageOption.SWEET_LAMB, StageOption.PANT_MAWR, StageOption.BIDNO_MOORLAND),
    WALES_B_REV(StageOption.GEUFRON_FOREST, StageOption.BIDNO_MOORLAND_REVERSE, StageOption.PANT_MAWR_REVERSE),
    ;

    private final StageOption longStage;
    private final StageOption firstShortStage;
    private final StageOption secondShortStage;

    StageConfiguration(StageOption longStage, StageOption firstShortStage, StageOption secondShortStage) {
        this.longStage = longStage;
        this.firstShortStage = firstShortStage;
        this.secondShortStage = secondShortStage;

    }


    public StageOption longStage() {
        return longStage;
    }

    public StageOption firstShort() {
        return firstShortStage;
    }

    public StageOption secondShort() {
        return this.secondShortStage;
    }

    public boolean containsOption(StageOption option) {
       return this.longStage == option || this.firstShort() == option || this.secondShort() == option;
    }

    public List<StageOption> getStages() {
        return List.of(longStage(), firstShort(), secondShort());
    }
}
