package io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation;

public enum ServiceArea {

    NONE("None"),
    SHORT("eShort"),
    MEDIUM("eMedium"),
    LONG("eLong");

    private final String id;

    ServiceArea(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }
}
