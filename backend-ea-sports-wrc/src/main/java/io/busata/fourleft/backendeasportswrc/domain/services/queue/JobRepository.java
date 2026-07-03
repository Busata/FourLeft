package io.busata.fourleft.backendeasportswrc.domain.services.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Recent jobs for the dashboard, newest first, filtered by an optional type
     * and status ({@code null} ignores either) and a ref LIKE pattern. The caller
     * always passes a concrete pattern ({@code %%} matches everything) so Postgres
     * can infer the parameter type — a null inside LOWER(...) would resolve to
     * LOWER(bytea).
     */
    @Query("""
            SELECT j FROM Job j
            WHERE (:type IS NULL OR j.type = :type)
              AND (:status IS NULL OR j.status = :status)
              AND LOWER(j.ref) LIKE LOWER(:searchPattern)
            ORDER BY j.id DESC
            """)
    List<Job> search(@Param("type") JobType type,
                           @Param("status") JobStatus status,
                           @Param("searchPattern") String searchPattern,
                           Pageable pageable);

    long countByStatus(JobStatus status);

    long countByType(JobType type);

    /** Prune terminal jobs older than a cutoff (keeps the table bounded). */
    @Modifying
    @Query(value = """
            DELETE FROM job
            WHERE status = :status AND created_at < now() - make_interval(hours => :hours)
            """, nativeQuery = true)
    int deleteByStatusOlderThanHours(@Param("status") String status, @Param("hours") int hours);

    /**
     * Claim the next runnable job of one type. Filtering by type lets the worker give each job type
     * its own concurrency budget, so a flood of one type (e.g. a full time-trial sweep) can't starve
     * another (e.g. club syncs). {@code FOR UPDATE SKIP LOCKED} means concurrent workers each grab a
     * different row instead of blocking on the same one. The returned entity is managed, so the caller
     * flipping it to RUNNING is flushed on transaction commit (which also releases the row lock).
     */
    @Query(value = """
            SELECT * FROM job
            WHERE status = 'PENDING' AND type = :type
            ORDER BY created_at
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """, nativeQuery = true)
    Optional<Job> claimNext(@Param("type") String type);

    /** True if this recurring target already has a job in flight (prevents pile-up). */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM job
                WHERE target_id = :targetId AND status IN ('PENDING', 'RUNNING')
            )
            """, nativeQuery = true)
    boolean hasActiveJobForTarget(@Param("targetId") Long targetId);

    /** Recover jobs whose worker died mid-flight: RUNNING for too long -> back to PENDING. */
    @Modifying
    @Query(value = """
            UPDATE job
            SET status = 'PENDING', locked_at = NULL
            WHERE status = 'RUNNING' AND locked_at < now() - make_interval(secs => :staleSeconds)
            """, nativeQuery = true)
    int requeueStale(@Param("staleSeconds") int staleSeconds);

    /**
     * Push a still-running job's stale clock forward. A long job (e.g. fetching a board with tens of
     * thousands of entries, paged 20 at a time) calls this as it makes progress so {@link #requeueStale}
     * doesn't mistake it for a crashed worker and run it again in parallel. A crashed worker stops
     * touching it, so genuine orphans are still recovered.
     */
    @Modifying
    @Query(value = "UPDATE job SET locked_at = now() WHERE id = :id AND status = 'RUNNING'", nativeQuery = true)
    int touchLock(@Param("id") long id);
}
