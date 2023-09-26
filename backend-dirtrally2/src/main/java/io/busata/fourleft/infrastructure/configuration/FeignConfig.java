package io.busata.fourleft.infrastructure.configuration;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("io.busata.fourleft.infrastructure.clients")
@Configuration
public class FeignConfig
{

}
