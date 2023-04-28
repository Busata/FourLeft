package io.busata.fourleft.api.models.discord;

import java.util.Objects;

public record DiscordGuildSummaryTo (
        String id,
        String name,
        String icon,
        boolean botJoined
){
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordGuildSummaryTo that = (DiscordGuildSummaryTo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
