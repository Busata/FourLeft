package io.busata.fourleft.backendacrally.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Enables {@code @Async} (used for best-effort Steam profile refresh on login). */
@Configuration
@EnableAsync
public class AsyncConfig {
}
