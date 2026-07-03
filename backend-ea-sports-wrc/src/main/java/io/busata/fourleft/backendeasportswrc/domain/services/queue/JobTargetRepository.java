package io.busata.fourleft.backendeasportswrc.domain.services.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.JobTarget;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobTargetRepository extends JpaRepository<JobTarget, Long> {

    Optional<JobTarget> findByTypeAndRef(JobType type, String ref);

    List<JobTarget> findByType(JobType type);

    /**
     * Claim targets that are due to run. Same SKIP LOCKED trick as the job queue:
     * each scheduler tick (across instances) takes a disjoint slice of due targets.
     * {@code :now} is bound from {@code ApplicationClock} rather than SQL {@code now()} so the
     * app clock stays authoritative (and tests can advance it).
     */
    @Query(value = """
            SELECT * FROM job_target
            WHERE enabled = true AND next_run_at <= :now
            ORDER BY next_run_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<JobTarget> claimDue(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
