package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord;

import io.busata.fourleft.api.easportswrc.models.DiscordUserInfoTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordUserTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Resolves discord ids to names for the operator pages. We only store ids (stable, unlike names); a failed lookup
 * (unknown user, rate limit) degrades to the bare id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordUserDirectory {
    private final DiscordGateway discordGateway;

    public DiscordUserInfoTo lookup(String discordId) {
        if (discordId == null) {
            return null;
        }
        try {
            DiscordUserTo user = discordGateway.getUser(discordId);
            return new DiscordUserInfoTo(discordId, user.username(), user.globalName(), avatarUrl(user));
        } catch (Exception e) {
            log.warn("Could not look up discord user {}: {}", discordId, e.getMessage());
            return new DiscordUserInfoTo(discordId, null, null, null);
        }
    }

    private static String avatarUrl(DiscordUserTo user) {
        return user.avatar() == null ? null : "https://cdn.discordapp.com/avatars/%s/%s.png?size=64".formatted(user.id(), user.avatar());
    }
}
