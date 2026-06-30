package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.work.queue.QueueProperties;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only views feeding the public work-queue status page. Covers every
 * {@link JobType} (club imports, time-trial imports, config cleanup, …).
 */
@RestController
@RequiredArgsConstructor
public class WorkQueueEndpoint {

    private final JobRepository jobRepository;
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

    public record SummaryView(boolean queueEnabled,
                              Map<String, Long> jobCountsByStatus,
                              Map<String, Long> jobCountsByType) {
    }

    public record JobView(Long id, String type, String ref, String status, int attempts,
                          Instant runAfter, Instant lockedAt, Instant createdAt,
                          Long targetId, String lastError) {
        static JobView from(Job j) {
            return new JobView(j.getId(), j.getType().name(), j.getRef(), j.getStatus().name(),
                    j.getAttempts(), j.getRunAfter(), j.getLockedAt(), j.getCreatedAt(),
                    j.getTargetId(), j.getLastError());
        }
    }
}
