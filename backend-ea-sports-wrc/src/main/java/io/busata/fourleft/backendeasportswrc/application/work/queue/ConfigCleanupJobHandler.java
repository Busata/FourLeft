package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordConfigurationCleanupService;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Prunes Discord configurations whose club (on Racenet) or channel (on Discord) no longer exists.
 * Runs as a single recurring target on a fixed daily cadence (see
 * {@link JobTargetService#syncSystemTargets()}).
 */
@Component
@RequiredArgsConstructor
public class ConfigCleanupJobHandler implements JobHandler {

    /** Single-instance target ref; there is exactly one cleanup target. */
    static final String REF = "discord-config-cleanup";

    private final DiscordConfigurationCleanupService cleanupService;

    @Override
    public JobType type() {
        return JobType.CONFIG_CLEANUP;
    }

    @Override
    public JobResult handle(Job job) {
        int removed = cleanupService.cleanupStaleConfigurations();
        // Fixed cadence (min == max), so the changed flag is informational only.
        return removed > 0 ? JobResult.dataChanged() : JobResult.noChange();
    }
}
