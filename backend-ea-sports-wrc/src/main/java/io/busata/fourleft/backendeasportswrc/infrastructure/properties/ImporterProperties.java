package io.busata.fourleft.backendeasportswrc.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fourleft.importer")
@Getter
@Setter
public class ImporterProperties {
    /** Upper bound on clubs importing concurrently, so a sync tick never fires off every syncable club at once. */
    private int maxConcurrentClubs = 5;
}
