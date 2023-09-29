package io.busata.fourleft.backendwrc.infrastructure.configuration;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("io.busata.fourleft.backendwrc.infrastructure.clients")
@Configuration
public class FeignConfig
{

}
