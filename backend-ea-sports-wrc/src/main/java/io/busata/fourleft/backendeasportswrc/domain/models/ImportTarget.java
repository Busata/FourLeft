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
 * A recurring thing we want to keep imported, plus its own refresh cadence.
 * <p>
 * Cadence is adaptive within [{@code minIntervalSec}, {@code maxIntervalSec}]:
 * after each run the interval shrinks when data changed and grows when it didn't,
 * so popular combinations stay hot and stale ones drift toward the ceiling on their
 * own. Set {@code minIntervalSec == maxIntervalSec} for a plain fixed cadence.
 */
@Entity
@Getter
@NoArgsConstructor
public class ImportTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_target_generator")
    @SequenceGenerator(name = "import_target_generator", sequenceName = "import_target_seq", allocationSize = 50)
    Long id;

    @Enumerated(EnumType.STRING)
    ImportType type;

    String ref;

    @Setter
    int intervalSec;

    int minIntervalSec;

    int maxIntervalSec;

    Instant nextRunAt;

    @Setter
    boolean enabled = true;

    public ImportTarget(ImportType type, String ref, int minIntervalSec, int maxIntervalSec) {
        this.type = type;
        this.ref = ref;
        this.minIntervalSec = minIntervalSec;
        this.maxIntervalSec = maxIntervalSec;
        this.intervalSec = minIntervalSec;
        this.nextRunAt = Instant.now();
        this.enabled = true;
    }
}
