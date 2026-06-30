package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.ImportTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Owns the {@code import_target} schedule: which things recur, how often, and adapting that. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportTargetService {

    private final ImportTargetRepository targetRepository;
    private final ClubConfigurationService clubConfigurationService;
    private final ImportJobService jobService;
    private final ImportQueueProperties properties;

    /**
     * Make sure every syncable club has an enabled CLUB target. This replaces the
     * legacy {@code findSyncableClubs} -> in-memory map seeding.
     */
    @Transactional
    public void syncClubTargets() {
        int interval = properties.getClubIntervalSeconds();
        List<ClubConfiguration> syncable = clubConfigurationService.findSyncableClubs();
        for (ClubConfiguration config : syncable) {
            targetRepository.findByTypeAndRef(ImportType.CLUB, config.getClubId())
                    .ifPresentOrElse(
                            existing -> {
                                if (!existing.isEnabled()) {
                                    existing.setEnabled(true);
                                    targetRepository.save(existing);
                                }
                            },
                            () -> targetRepository.save(
                                    // Fixed cadence for clubs: min == max.
                                    new ImportTarget(ImportType.CLUB, config.getClubId(), interval, interval)));
        }

        // Prune the other direction: a club that is no longer syncable (e.g. removed after a 404)
        // must stop being scheduled, otherwise enqueueDueTargets() keeps minting jobs for it forever.
        Set<String> syncableRefs = syncable.stream().map(ClubConfiguration::getClubId).collect(Collectors.toSet());
        for (ImportTarget target : targetRepository.findByType(ImportType.CLUB)) {
            if (target.isEnabled() && !syncableRefs.contains(target.getRef())) {
                target.setEnabled(false);
                targetRepository.save(target);
            }
        }
    }

    /**
     * Turn due targets into jobs. Claims a batch with SKIP LOCKED, enqueues one job
     * each (skipping any target that still has a job in flight), and reschedules the
     * target by its current cadence.
     *
     * @return number of jobs enqueued
     */
    @Transactional
    public int enqueueDueTargets() {
        List<ImportTarget> due = targetRepository.claimDue(properties.getScheduleBatchSize());
        int enqueued = 0;
        for (ImportTarget target : due) {
            boolean created = jobService
                    .enqueueForTarget(target.getType(), target.getRef(), target.getId())
                    .isPresent();
            if (created) {
                enqueued++;
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
