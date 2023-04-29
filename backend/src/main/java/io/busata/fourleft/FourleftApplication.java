package io.busata.fourleft;

import io.busata.fourleft.infrastructure.configuration.AsyncConfig;
import io.busata.fourleft.infrastructure.configuration.FeignConfig;
import io.busata.fourleft.infrastructure.configuration.ImageGenerationConfig;
import io.busata.fourleft.infrastructure.configuration.KeycloakConfig;
import io.busata.fourleft.infrastructure.configuration.MessagingConfig;
import io.busata.fourleft.infrastructure.configuration.SchedulingConfig;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
        AsyncConfig.class,
        FeignConfig.class,
        ImageGenerationConfig.class,
        KeycloakConfig.class,
        MessagingConfig.class,
        SchedulingConfig.class,
        SecurityConfig.class
})
public class FourleftApplication {

    public static void main(String[] args) {
        SpringApplication.run(FourleftApplication.class, args);
    }

}
