package io.busata.fourleft;

import io.busata.fourleft.infrastructure.configuration.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
        AsyncConfig.class,
        FeignConfig.class,
        CachingConfig.class,
        ImageGenerationConfig.class,
        InterceptorsConfig.class,
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
