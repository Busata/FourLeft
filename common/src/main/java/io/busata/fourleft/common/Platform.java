package io.busata.fourleft.common;

public enum Platform {
    PC,
    PLAYSTATION,
    XBOX,
    EPIC,
    EA_APP,
    STEAM,
    STEAM_DECK,
    OTHER,
    UNKNOWN;


    public static Platform fromEASportsWRCId(Long id) {
        if (id == 1L) {
            return Platform.XBOX;
        } else if (id == 2L) {
            return Platform.PLAYSTATION;
        } else if (id == 3L) {
            return Platform.STEAM;
        } else if (id == 6L) {
            return Platform.PC;
        } else if (id == 7L) {
            return Platform.PC;
        } else if (id == 14L) {
            return Platform.EA_APP;
        } else if (id == 65L) {
            return Platform.PC;
        } else if (id == 128L) {
            return Platform.STEAM;
        }
        return Platform.UNKNOWN;
    }


}
