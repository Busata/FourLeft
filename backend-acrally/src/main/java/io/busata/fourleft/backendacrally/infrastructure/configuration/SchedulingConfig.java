package io.busata.fourleft.backendacrally.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} (used for the stale-session sweep). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
