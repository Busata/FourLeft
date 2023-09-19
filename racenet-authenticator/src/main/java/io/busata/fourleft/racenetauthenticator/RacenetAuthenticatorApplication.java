package io.busata.fourleft.racenetauthenticator;

import io.busata.fourleft.racenetauthenticator.infrastructure.configuration.FeignConfig;
import io.busata.fourleft.racenetauthenticator.infrastructure.configuration.SchedulingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
        FeignConfig.class,
        SchedulingConfig.class,
})
public class RacenetAuthenticatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RacenetAuthenticatorApplication.class, args);
    }

}
