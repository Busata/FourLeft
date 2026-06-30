package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "import-queue")
@Getter
@Setter
public class ImportQueueProperties {

    /** Master switch for the new worker system. Off => the legacy importer keeps running. */
    private boolean enabled = false;

    /** Fixed cadence for CLUB targets, in seconds. */
    private int clubIntervalSeconds = 30;

    /** Max jobs a single worker tick drains before yielding. */
    private int batchSize = 25;

    /** Max due targets turned into jobs per scheduler tick. */
    private int scheduleBatchSize = 100;

    /** Attempts (inclusive) before a job is marked FAILED. */
    private int maxAttempts = 3;

    /** A RUNNING job whose worker vanished is requeued after this many seconds. */
    private int staleJobSeconds = 600;

    /** Backoff base (seconds) applied as attempts * base before a retry. */
    private int retryBackoffSeconds = 30;
}
