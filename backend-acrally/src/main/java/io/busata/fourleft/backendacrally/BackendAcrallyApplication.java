package io.busata.fourleft.backendacrally;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendAcrallyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendAcrallyApplication.class, args);
    }

}
