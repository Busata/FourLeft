package io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation;

public enum SurfaceDegradation {

    NONE(0),
    LOW(25),
    MEDIUM(50),
    HIGH(75),
    MAX(100);

    private final int degradation;

    SurfaceDegradation(int degradation) {
        this.degradation = degradation;
    }

    public int degradation() {
        return this.degradation;
    }
}
