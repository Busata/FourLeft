package io.busata.fourleft.gateway.racenet;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class FeignConfiguration {

    @Bean()
    public ErrorDecoder errorDecoder() {
        return new Custom5xxErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(3L), 10);
    }
}
