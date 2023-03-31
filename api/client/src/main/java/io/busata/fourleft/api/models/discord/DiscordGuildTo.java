package io.busata.fourleft.api.models.discord;

import java.util.Objects;

public record DiscordGuildTo(
    String id,
    String name,
    String icon,
    boolean owner,

    int permissions
    ) {

    public boolean canManageServer() {
        return (permissions() & 0x20) == 0x20;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordGuildTo that = (DiscordGuildTo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
