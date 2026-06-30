package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.JobTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Owns the {@code job_target} schedule: which things recur, how often, and adapting that. */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobTargetService {

    private final JobTargetRepository targetRepository;
    private final ClubConfigurationService clubConfigurationService;
    private final JobService jobService;
    private final JobDispatcher jobDispatcher;
    private final QueueProperties properties;

    /**
     * Make sure every syncable club has an enabled CLUB target. This replaces the
     * legacy {@code findSyncableClubs} -> in-memory map seeding.
     */
    @Transactional
    public void syncClubTargets() {
        int interval = properties.getClubIntervalSeconds();
        List<ClubConfiguration> syncable = clubConfigurationService.findSyncableClubs();
        for (ClubConfiguration config : syncable) {
            targetRepository.findByTypeAndRef(JobType.CLUB, config.getClubId())
                    .ifPresentOrElse(
                            existing -> {
                                boolean dirty = false;
                                if (!existing.isEnabled()) {
                                    existing.setEnabled(true);
                                    dirty = true;
                                }
                                // Keep the cadence in sync with config (clubs are fixed: min == max).
                                if (existing.getIntervalSec() != interval
                                        || existing.getMinIntervalSec() != interval
                                        || existing.getMaxIntervalSec() != interval) {
                                    existing.setIntervalSec(interval);
                                    existing.setMinIntervalSec(interval);
                                    existing.setMaxIntervalSec(interval);
                                    dirty = true;
                                }
                                if (dirty) {
                                    targetRepository.save(existing);
                                }
                            },
                            () -> targetRepository.save(
                                    // Fixed cadence for clubs: min == max.
                                    new JobTarget(JobType.CLUB, config.getClubId(), interval, interval)));
        }

        // Prune the other direction: a club that is no longer syncable (e.g. removed after a 404)
        // must stop being scheduled, otherwise enqueueDueTargets() keeps minting jobs for it forever.
        Set<String> syncableRefs = syncable.stream().map(ClubConfiguration::getClubId).collect(Collectors.toSet());
        for (JobTarget target : targetRepository.findByType(JobType.CLUB)) {
            if (target.isEnabled() && !syncableRefs.contains(target.getRef())) {
                target.setEnabled(false);
                targetRepository.save(target);
            }
        }
    }

    /**
     * Make sure the system maintenance targets exist. Currently just the single Discord
     * configuration cleanup target, on a fixed daily cadence.
     */
    @Transactional
    public void syncSystemTargets() {
        int interval = properties.getConfigCleanupIntervalSeconds();
        targetRepository.findByTypeAndRef(JobType.CONFIG_CLEANUP, ConfigCleanupJobHandler.REF)
                .ifPresentOrElse(
                        existing -> {
                            boolean dirty = false;
                            if (!existing.isEnabled()) {
                                existing.setEnabled(true);
                                dirty = true;
                            }
                            // Fixed cadence: min == max == interval.
                            if (existing.getIntervalSec() != interval
                                    || existing.getMinIntervalSec() != interval
                                    || existing.getMaxIntervalSec() != interval) {
                                existing.setIntervalSec(interval);
                                existing.setMinIntervalSec(interval);
                                existing.setMaxIntervalSec(interval);
                                dirty = true;
                            }
                            if (dirty) {
                                targetRepository.save(existing);
                            }
                        },
                        () -> targetRepository.save(
                                new JobTarget(JobType.CONFIG_CLEANUP, ConfigCleanupJobHandler.REF, interval, interval)));
    }

    /**
     * Turn due targets into jobs. Claims a batch with SKIP LOCKED and, for each target
     * that actually has work to do (and has no job already in flight), enqueues one job.
     * Every claimed target is rescheduled by its cadence whether or not it produced a
     * job, so no-op visits never reach the queue.
     *
     * @return number of jobs enqueued
     */
    @Transactional
    public int enqueueDueTargets() {
        List<JobTarget> due = targetRepository.claimDue(properties.getScheduleBatchSize());
        int enqueued = 0;
        for (JobTarget target : due) {
            if (jobDispatcher.shouldEnqueue(target.getType(), target.getRef())) {
                boolean created = jobService
                        .enqueueForTarget(target.getType(), target.getRef(), target.getId())
                        .isPresent();
                if (created) {
                    enqueued++;
                }
            }
            targetRepository.reschedule(target.getId());
        }
        return enqueued;
    }

    /**
     * Move a target's cadence within its [min, max] bounds after a run: halve toward
     * the floor when data changed, double toward the ceiling when it didn't. A no-op
     * for fixed-cadence targets (min == max).
     */
    @Transactional
    public void applyAdaptiveCadence(Long targetId, boolean changed) {
        targetRepository.findById(targetId).ifPresent(target -> {
            int current = target.getIntervalSec();
            int next = changed
                    ? Math.max(target.getMinIntervalSec(), current / 2)
                    : Math.min(target.getMaxIntervalSec(), current * 2);
            if (next != current) {
                target.setIntervalSec(next);
                targetRepository.save(target);
            }
        });
    }
}
