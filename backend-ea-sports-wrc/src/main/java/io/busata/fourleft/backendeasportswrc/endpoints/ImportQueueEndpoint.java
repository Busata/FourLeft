package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.importer.queue.ImportQueueProperties;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportJobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.ImportJobRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.ImportTargetRepository;
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
 * Read-only views feeding the public import-queue dashboard (see
 * {@code static/import-queue.html}). Scoped to {@link ImportType#CLUB} for now.
 */
@RestController
@RequiredArgsConstructor
public class ImportQueueEndpoint {

    private static final ImportType FOCUS = ImportType.CLUB;

    private final ImportJobRepository jobRepository;
    private final ImportTargetRepository targetRepository;
    private final ImportQueueProperties properties;

    @GetMapping("/api_v2/import-queue/summary")
    public SummaryView summary() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ImportJobStatus status : ImportJobStatus.values()) {
            counts.put(status.name(), jobRepository.countByTypeAndStatus(FOCUS, status));
        }
        long targets = targetRepository.findByType(FOCUS).size();
        return new SummaryView(properties.isEnabled(), targets, counts);
    }

    @GetMapping("/api_v2/import-queue/jobs")
    public List<JobView> jobs(@RequestParam(defaultValue = "100") int limit) {
        int capped = Math.min(Math.max(limit, 1), 500);
        return jobRepository.findByTypeOrderByIdDesc(FOCUS, PageRequest.of(0, capped))
                .stream().map(JobView::from).toList();
    }

    @GetMapping("/api_v2/import-queue/targets")
    public List<TargetView> targets() {
        return targetRepository.findByType(FOCUS)
                .stream()
                .sorted((a, b) -> a.getRef().compareToIgnoreCase(b.getRef()))
                .map(TargetView::from).toList();
    }

    public record SummaryView(boolean queueEnabled, long targetCount, Map<String, Long> jobCountsByStatus) {
    }

    public record JobView(Long id, String ref, String status, int attempts,
                          Instant runAfter, Instant lockedAt, Instant createdAt,
                          Long targetId, String lastError) {
        static JobView from(ImportJob j) {
            return new JobView(j.getId(), j.getRef(), j.getStatus().name(), j.getAttempts(),
                    j.getRunAfter(), j.getLockedAt(), j.getCreatedAt(), j.getTargetId(), j.getLastError());
        }
    }

    public record TargetView(Long id, String ref, int intervalSec, int minIntervalSec,
                             int maxIntervalSec, Instant nextRunAt, boolean enabled) {
        static TargetView from(ImportTarget t) {
            return new TargetView(t.getId(), t.getRef(), t.getIntervalSec(), t.getMinIntervalSec(),
                    t.getMaxIntervalSec(), t.getNextRunAt(), t.isEnabled());
        }
    }
}
