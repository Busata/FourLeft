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

import java.time.LocalDateTime;

/**
 * A recurring thing we keep imported, plus its next-due time.
 * <p>
 * There is no fixed cadence: {@code nextRunAt} is DERIVED from the target's own domain state after
 * each run (for clubs, {@code ClubService.nextDueAt} — the earliest moment work becomes due again),
 * so active targets refresh promptly and idle ones drift out on their own.
 */
@Entity
@Getter
@NoArgsConstructor
public class JobTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_target_generator")
    @SequenceGenerator(name = "job_target_generator", sequenceName = "job_target_seq", allocationSize = 50)
    Long id;

    @Enumerated(EnumType.STRING)
    JobType type;

    String ref;

    @Setter
    LocalDateTime nextRunAt;

    @Setter
    boolean enabled = true;

    public JobTarget(JobType type, String ref, LocalDateTime nextRunAt) {
        this.type = type;
        this.ref = ref;
        this.nextRunAt = nextRunAt;
        this.enabled = true;
    }
}
