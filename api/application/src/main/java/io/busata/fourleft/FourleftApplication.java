package io.busata.fourleft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
public class FourleftApplication {

    public static void main(String[] args) {
        SpringApplication.run(FourleftApplication.class, args);
    }

}
