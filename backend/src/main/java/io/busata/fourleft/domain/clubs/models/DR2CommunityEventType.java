package io.busata.fourleft.domain.clubs.models;

public enum DR2CommunityEventType {
    Daily("Daily"),
    Weekly("Weekly"),
    Monthly("Monthly");

    private String displayName;

    DR2CommunityEventType(String displayName) {
        this.displayName = displayName;
    }
}
