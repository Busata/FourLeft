package io.busata.fourleft.backendeasportswrc;

import io.busata.fourleft.backendeasportswrc.infrastructure.configuration.AsyncEventsConfig;
import io.busata.fourleft.backendeasportswrc.infrastructure.configuration.FeignConfig;
import io.busata.fourleft.backendeasportswrc.infrastructure.configuration.FlywayConfiguration;
import io.busata.fourleft.backendeasportswrc.infrastructure.configuration.SchedulingConfig;
import io.busata.fourleft.backendeasportswrc.infrastructure.rabbitmq.RabbitMQConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
        FeignConfig.class,
        RabbitMQConfiguration.class,
        SchedulingConfig.class,
        FlywayConfiguration.class,
        AsyncEventsConfig.class
})
public class BackendEASportsWRCApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendEASportsWRCApplication.class, args);
    }

}
