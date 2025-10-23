package io.busata.fourleft.backendeasportswrc.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "club-export")
@Getter
@Setter
public class ClubExportProperties {
    private String outputDirectory = "/tmp/club-exports";
    private boolean schedulingEnabled = true;
}
