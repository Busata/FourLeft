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
 * recurring {@link ImportTarget} scheduler or enqueued ad-hoc (e.g. "import club X").
 * Throwaway: once {@code DONE} it can be pruned; it carries no long-lived state.
 */
@Entity
@Getter
@NoArgsConstructor
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_job_generator")
    @SequenceGenerator(name = "import_job_generator", sequenceName = "import_job_seq", allocationSize = 50)
    Long id;

    @Enumerated(EnumType.STRING)
    ImportType type;

    String ref;

    @Setter
    @Enumerated(EnumType.STRING)
    ImportJobStatus status = ImportJobStatus.PENDING;

    @Setter
    int attempts = 0;

    @Setter
    Instant runAfter;

    @Setter
    Instant lockedAt;

    Instant createdAt;

    /** Originating recurring target, or {@code null} for an ad-hoc one-off job. */
    Long targetId;

    @Setter
    String lastError;

    public ImportJob(ImportType type, String ref, Long targetId) {
        this.type = type;
        this.ref = ref;
        this.targetId = targetId;
        this.status = ImportJobStatus.PENDING;
        this.attempts = 0;
        this.runAfter = Instant.now();
        this.createdAt = Instant.now();
    }

    public void markRunning() {
        this.status = ImportJobStatus.RUNNING;
        this.lockedAt = Instant.now();
        this.attempts = this.attempts + 1;
    }

    public void markDone() {
        this.status = ImportJobStatus.DONE;
        this.lockedAt = null;
    }
}
