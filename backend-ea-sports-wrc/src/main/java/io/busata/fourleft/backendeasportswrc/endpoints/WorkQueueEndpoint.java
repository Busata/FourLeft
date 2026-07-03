package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.work.queue.QueueProperties;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.JobTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only views feeding the work-queue status page: which things are importing now
 * ({@code jobs?status=RUNNING}) and when each is next due ({@code targets}).
 */
@RestController
@RequiredArgsConstructor
public class WorkQueueEndpoint {

    private final JobRepository jobRepository;
    private final JobTargetRepository jobTargetRepository;
    private final QueueProperties properties;

    /** Whether the queue is enabled + per-status and per-type totals (drive the filter chips). */
    @GetMapping("/api_v2/work-queue/summary")
    public SummaryView summary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (JobStatus status : JobStatus.values()) {
            byStatus.put(status.name(), jobRepository.countByStatus(status));
        }
        Map<String, Long> byType = new LinkedHashMap<>();
        for (JobType type : JobType.values()) {
            byType.put(type.name(), jobRepository.countByType(type));
        }
        return new SummaryView(properties.isEnabled(), byStatus, byType);
    }

    /**
     * Recent jobs, newest first, optionally filtered by type, status and/or a ref search.
     *
     * @param type   optional exact job-type filter (CLUB/TT/CONFIG_CLEANUP)
     * @param status optional exact status filter (PENDING/RUNNING/DONE/FAILED)
     * @param search optional case-insensitive substring match on the ref
     */
    @GetMapping("/api_v2/work-queue/jobs")
    public List<JobView> jobs(@RequestParam(defaultValue = "100") int limit,
                              @RequestParam(required = false) JobType type,
                              @RequestParam(required = false) JobStatus status,
                              @RequestParam(required = false) String search) {
        int capped = Math.min(Math.max(limit, 1), 500);
        String term = (search == null) ? "" : search.trim();
        String searchPattern = "%" + term + "%"; // empty term -> "%%" matches everything
        return jobRepository.search(type, status, searchPattern, PageRequest.of(0, capped))
                .stream().map(JobView::from).toList();
    }

    /**
     * The recurring targets and each one's next-due time — "when is club X next scheduled".
     * Ordered soonest-due first.
     *
     * @param type optional exact job-type filter (CLUB)
     */
    @GetMapping("/api_v2/work-queue/targets")
    public List<TargetView> targets(@RequestParam(required = false) JobType type) {
        List<JobTarget> targets = (type == null)
                ? jobTargetRepository.findAll()
                : jobTargetRepository.findByType(type);
        return targets.stream()
                .sorted(Comparator.comparing(JobTarget::getNextRunAt))
                .map(TargetView::from)
                .toList();
    }

    public record TargetView(Long id, String type, String ref, Instant nextRunAt, boolean enabled) {
        static TargetView from(JobTarget t) {
            // nextRunAt is a LocalDateTime in the app's UTC clock; emit it as a zoned Instant so the
            // client parses it in UTC (a bare LocalDateTime serializes without a zone and is read as local).
            return new TargetView(t.getId(), t.getType().name(), t.getRef(),
                    t.getNextRunAt().toInstant(ZoneOffset.UTC), t.isEnabled());
        }
    }

    public record SummaryView(boolean queueEnabled,
                              Map<String, Long> jobCountsByStatus,
                              Map<String, Long> jobCountsByType) {
    }

    /**
     * A job for the status table. Beyond identity/status it reports what a completed run did
     * ({@code outcome}, {@code changed}, item counts), how long it took ({@code durationMs}), how
     * long it waited to be picked up ({@code waitMs}), and whether it was recovered from a crashed
     * worker ({@code attempts > 1 -> recovered}).
     */
    public record JobView(Long id, String type, String ref, String status,
                          Instant createdAt, Instant startedAt, Instant finishedAt,
                          Long durationMs, Long waitMs, int attempts, boolean recovered,
                          String outcome, Boolean changed,
                          Integer leaderboardsUpdated, Integer standingsUpdated, Integer entriesImported,
                          Long targetId, String lastError) {
        static JobView from(Job j) {
            Instant started = j.getStartedAt();
            Instant finished = j.getFinishedAt();
            Long durationMs = (started != null && finished != null)
                    ? Duration.between(started, finished).toMillis() : null;
            Long waitMs = (started != null)
                    ? Duration.between(j.getCreatedAt(), started).toMillis() : null;
            int attempts = (j.getAttempts() == null) ? 0 : j.getAttempts();
            return new JobView(j.getId(), j.getType().name(), j.getRef(), j.getStatus().name(),
                    j.getCreatedAt(), started, finished, durationMs, waitMs, attempts, attempts > 1,
                    (j.getOutcome() == null) ? null : j.getOutcome().name(), j.getChanged(),
                    j.getLeaderboardsUpdated(), j.getStandingsUpdated(), j.getEntriesImported(),
                    j.getTargetId(), j.getLastError());
        }
    }
}
