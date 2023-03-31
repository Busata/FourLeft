package io.busata.wrcserver.importer.client.models;

import java.util.Objects;
import java.util.Optional;

public record WRCRallyEventEntryTo(
        String id,
        String name,
        boolean active,
        WRCRallyEventStatus status
) {


    public boolean hasEventEnded() {
        return !this.active || Objects.equals(status.id(), "25");
    }
}
