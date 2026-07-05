package io.busata.fourleft.backendeasportswrc.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "time-trial-export")
@Getter
@Setter
public class TimeTrialExportProperties {
    private String outputDirectory = "/tmp/tt-exports";
}
