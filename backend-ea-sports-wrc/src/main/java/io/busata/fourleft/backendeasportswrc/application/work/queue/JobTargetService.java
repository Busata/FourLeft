package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.JobTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobTargetRepository;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Owns the {@code job_target} schedule: which things recur and when each is next due. Unlike the
 * original fixed-cadence version, {@code next_run_at} is DERIVED from each target's own state
 * (via {@link JobDispatcher#nextRunAt}), so idle targets drift out on their own.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobTargetService {

    private final JobTargetRepository targetRepository;
    private final ClubConfigurationService clubConfigurationService;
    private final JobService jobService;
    private final JobDispatcher jobDispatcher;
    private final QueueProperties properties;

    /** Make sure every syncable club has an enabled CLUB target; disable targets for the rest. */
    @Transactional
    public void syncClubTargets() {
        List<ClubConfiguration> syncable = clubConfigurationService.findSyncableClubs();
        for (ClubConfiguration config : syncable) {
            targetRepository.findByTypeAndRef(JobType.CLUB, config.getClubId())
                    .ifPresentOrElse(
                            existing -> {
                                if (!existing.isEnabled()) {
                                    existing.setEnabled(true);
                                    targetRepository.save(existing);
                                }
                            },
                            // New target is due immediately so a fresh club imports on the next tick.
                            () -> targetRepository.save(
                                    new JobTarget(JobType.CLUB, config.getClubId(), ApplicationClock.now())));
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
     * Turn due targets into jobs. Claims a batch with SKIP LOCKED and, for each target with real
     * work to do (and no job already in flight), enqueues one job and holds the target off for a
     * short grace so it isn't re-claimed mid-import ({@code process()} sets the accurate next-due on
     * completion). Targets with nothing to do are pushed straight to their derived next-due.
     *
     * @return number of jobs enqueued
     */
    @Transactional
    public int enqueueDueTargets() {
        List<JobTarget> due = targetRepository.claimDue(ApplicationClock.now(), properties.getScheduleBatchSize());
        int enqueued = 0;
        for (JobTarget target : due) {
            if (jobDispatcher.shouldEnqueue(target.getType(), target.getRef())) {
                boolean created = jobService
                        .enqueueForTarget(target.getType(), target.getRef(), target.getId())
                        .isPresent();
                if (created) {
                    enqueued++;
                }
                target.setNextRunAt(ApplicationClock.now().plusSeconds(properties.getInFlightGraceSeconds()));
            } else {
                target.setNextRunAt(jobDispatcher.nextRunAt(target.getType(), target.getRef()));
            }
            targetRepository.save(target);
        }
        return enqueued;
    }

    /** Recompute a target's next-due from its current (post-import) state. */
    @Transactional
    public void rescheduleFromState(Long targetId) {
        targetRepository.findById(targetId).ifPresent(target -> {
            target.setNextRunAt(jobDispatcher.nextRunAt(target.getType(), target.getRef()));
            targetRepository.save(target);
        });
    }
}
