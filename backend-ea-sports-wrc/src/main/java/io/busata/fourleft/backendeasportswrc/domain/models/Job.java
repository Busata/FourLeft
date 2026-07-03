package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single unit of work ("import this one thing now"). Created either by the
 * recurring {@link JobTarget} scheduler or enqueued ad-hoc (e.g. "import club X").
 * Runs exactly once — success {@code DONE}, failure {@code FAILED} — with no retry;
 * the target's schedule brings the work back around. Throwaway: terminal jobs are pruned.
 */
@Entity
@Getter
@NoArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_generator")
    @SequenceGenerator(name = "job_generator", sequenceName = "job_seq", allocationSize = 50)
    Long id;

    @Enumerated(EnumType.STRING)
    JobType type;

    String ref;

    @Setter
    @Enumerated(EnumType.STRING)
    JobStatus status = JobStatus.PENDING;

    /** When a worker claimed it (RUNNING); used only to recover jobs orphaned by a crashed worker. */
    @Setter
    Instant lockedAt;

    Instant createdAt;

    /** Originating recurring target, or {@code null} for an ad-hoc one-off job. */
    Long targetId;

    @Setter
    String lastError;

    public Job(JobType type, String ref, Long targetId) {
        this.type = type;
        this.ref = ref;
        this.targetId = targetId;
        this.status = JobStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markRunning() {
        this.status = JobStatus.RUNNING;
        this.lockedAt = Instant.now();
    }

    public void markDone() {
        this.status = JobStatus.DONE;
        this.lockedAt = null;
    }
}
