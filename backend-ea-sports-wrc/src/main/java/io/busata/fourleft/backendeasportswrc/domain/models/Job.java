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

    /**
     * When the worker began the (last) run. Unlike {@link #lockedAt} it survives completion, so
     * {@code finishedAt - startedAt} gives the run duration and {@code startedAt - createdAt} the
     * time the job waited in the queue.
     */
    Instant startedAt;

    /** When it reached a terminal state (DONE/FAILED). */
    Instant finishedAt;

    /** How many times a worker has started this job; {@code > 1} means it was recovered after a crash. */
    Integer attempts = 0;

    /** What the run did — null until terminal (and stays null for a hard FAILED). */
    @Enumerated(EnumType.STRING)
    JobOutcome outcome;

    /** Whether the run altered stored data (vs a no-op refresh); null until done. */
    Boolean changed;

    Integer leaderboardsUpdated;

    Integer standingsUpdated;

    Integer entriesImported;

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
        this.attempts = 0;
    }

    public void markRunning() {
        this.status = JobStatus.RUNNING;
        this.lockedAt = Instant.now();
        this.startedAt = Instant.now();
        this.attempts = (this.attempts == null ? 0 : this.attempts) + 1;
    }

    public void markDone() {
        this.status = JobStatus.DONE;
        this.lockedAt = null;
        this.finishedAt = Instant.now();
    }

    public void markFailed() {
        this.status = JobStatus.FAILED;
        this.lockedAt = null;
        this.finishedAt = Instant.now();
    }

    /** Record what the completed run did. Domain-typed so the entity stays free of the queue layer. */
    public void recordOutcome(JobOutcome outcome, boolean changed,
                              int leaderboardsUpdated, int standingsUpdated, int entriesImported) {
        this.outcome = outcome;
        this.changed = changed;
        this.leaderboardsUpdated = leaderboardsUpdated;
        this.standingsUpdated = standingsUpdated;
        this.entriesImported = entriesImported;
    }
}
