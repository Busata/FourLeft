package io.busata.fourleft.backendeasportswrc.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class AsyncEventsConfig {
    public class AsynchronousSpringEventsConfig {
        @Bean(name = "applicationEventMulticaster")
        public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
            SimpleApplicationEventMulticaster eventMulticaster =
                    new SimpleApplicationEventMulticaster();

            eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
            return eventMulticaster;
        }
    }
}
