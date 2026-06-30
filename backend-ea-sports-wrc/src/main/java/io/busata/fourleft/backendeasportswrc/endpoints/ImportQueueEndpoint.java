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
 * Read-only views feeding the public import-queue status page. Scoped to
 * {@link JobType#CLUB} for now.
 */
@RestController
@RequiredArgsConstructor
public class ImportQueueEndpoint {

    private static final JobType FOCUS = JobType.CLUB;

    private final JobRepository jobRepository;
    private final QueueProperties properties;

    /** Whether the queue is enabled + per-status totals (drive the filter chips). */
    @GetMapping("/api_v2/import-queue/summary")
    public SummaryView summary() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (JobStatus status : JobStatus.values()) {
            counts.put(status.name(), jobRepository.countByTypeAndStatus(FOCUS, status));
        }
        return new SummaryView(properties.isEnabled(), counts);
    }

    /**
     * Recent jobs, newest first, optionally filtered by status and/or club-id search.
     *
     * @param status optional exact status filter (PENDING/RUNNING/DONE/FAILED)
     * @param search optional case-insensitive substring match on the club id
     */
    @GetMapping("/api_v2/import-queue/jobs")
    public List<JobView> jobs(@RequestParam(defaultValue = "100") int limit,
                              @RequestParam(required = false) JobStatus status,
                              @RequestParam(required = false) String search) {
        int capped = Math.min(Math.max(limit, 1), 500);
        String term = (search == null) ? "" : search.trim();
        String searchPattern = "%" + term + "%"; // empty term -> "%%" matches everything
        return jobRepository.search(FOCUS, status, searchPattern, PageRequest.of(0, capped))
                .stream().map(JobView::from).toList();
    }

    public record SummaryView(boolean queueEnabled, Map<String, Long> jobCountsByStatus) {
    }

    public record JobView(Long id, String ref, String status, int attempts,
                          Instant runAfter, Instant lockedAt, Instant createdAt,
                          Long targetId, String lastError) {
        static JobView from(Job j) {
            return new JobView(j.getId(), j.getRef(), j.getStatus().name(), j.getAttempts(),
                    j.getRunAfter(), j.getLockedAt(), j.getCreatedAt(), j.getTargetId(), j.getLastError());
        }
    }
}
