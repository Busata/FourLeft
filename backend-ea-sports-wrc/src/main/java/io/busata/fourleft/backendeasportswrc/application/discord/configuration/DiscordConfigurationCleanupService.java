package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import feign.FeignException;
import io.busata.fourleft.api.easportswrc.events.ConfigurationUpdatedEvent;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.RacenetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Removes Discord configurations that point at a club which no longer exists on Racenet, or at a
 * Discord channel that has been deleted. Either dangling reference makes the configuration unusable
 * (e.g. result endpoints fail to resolve the club), so the configuration is dropped.
 * <p>
 * Club existence is verified against Racenet rather than the local {@code Club} table: a club that
 * was deleted on the EA side keeps its local row until something prunes it, so the local table
 * cannot detect that case. A 404 from Racenet is the authoritative signal that the club is gone.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordConfigurationCleanupService {

    private static final Long NOTICE_CHANNEL_ID = 1173372471207018576L;

    private final DiscordClubConfigurationService configurationService;
    private final RacenetGateway racenetGateway;
    private final DiscordGateway discordGateway;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * @return the number of stale configurations removed in this run.
     */
    public int cleanupStaleConfigurations() {
        List<DiscordClubConfiguration> configurations = configurationService.getConfigurations();

        log.info("Checking {} Discord configuration(s) for stale clubs/channels", configurations.size());

        // Multiple channels can point at the same club; only probe Racenet once per club per run.
        Map<String, Boolean> clubExistsCache = new HashMap<>();

        int removed = 0;
        for (DiscordClubConfiguration configuration : configurations) {
            String reason = findRemovalReason(configuration, clubExistsCache);
            if (reason != null) {
                remove(configuration, reason);
                removed++;
            }
        }

        if (removed > 0) {
            eventPublisher.publishEvent(new ConfigurationUpdatedEvent());
        }

        log.info("Discord configuration cleanup finished, removed {} stale configuration(s)", removed);
        return removed;
    }

    private String findRemovalReason(DiscordClubConfiguration configuration, Map<String, Boolean> clubExistsCache) {
        boolean clubExists = clubExistsCache.computeIfAbsent(configuration.getClubId(), this::clubExistsOnRacenet);
        if (!clubExists) {
            return "club %s no longer exists on Racenet".formatted(configuration.getClubId());
        }
        if (!channelExists(configuration.getChannelId())) {
            return "channel %s no longer exists".formatted(configuration.getChannelId());
        }
        return null;
    }

    private boolean clubExistsOnRacenet(String clubId) {
        try {
            racenetGateway.getClubDetail(clubId).join();
            return true;
        } catch (RuntimeException e) {
            if (isNotFound(e)) {
                return false;
            }
            // Auth / rate-limit / transient error: don't risk deleting a valid configuration.
            log.warn("Could not verify club {} on Racenet; keeping configuration", clubId, e);
            return true;
        }
    }

    private boolean channelExists(Long channelId) {
        try {
            discordGateway.getChannel(channelId);
            return true;
        } catch (FeignException e) {
            if (e.status() == 404) {
                return false;
            }
            // Permission / rate-limit / transient error: don't risk deleting a valid configuration.
            log.warn("Could not verify Discord channel {} (status {}); keeping configuration", channelId, e.status());
            return true;
        }
    }

    /**
     * A 404 anywhere in the cause chain means the resource no longer exists upstream. Anything else
     * is treated as a (potentially transient) failure that should not trigger a deletion.
     */
    private boolean isNotFound(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof FeignException feignException && feignException.status() == 404) {
                return true;
            }
        }
        return false;
    }

    private void remove(DiscordClubConfiguration configuration, String reason) {
        log.warn("Removing Discord configuration for channel {} / club {}: {}", configuration.getChannelId(), configuration.getClubId(), reason);
        configurationService.removeConfiguration(configuration.getChannelId(), configuration.getClubId());
        notifyRemoval(configuration, reason);
    }

    private void notifyRemoval(DiscordClubConfiguration configuration, String reason) {
        try {
            discordGateway.createMessage(NOTICE_CHANNEL_ID, new SimpleDiscordMessageTo(
                    "Auto-removed configuration for channelId (%s), club (%s): %s.".formatted(configuration.getChannelId(), configuration.getClubId(), reason),
                    List.of()
            ));
        } catch (FeignException e) {
            log.warn("Could not post cleanup notice for channel {} (status {})", configuration.getChannelId(), e.status());
        }
    }
}
