package io.busata.fourleft.backendeasportswrc.infrastructure.configuration;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("io.busata.fourleft.backendeasportswrc.infrastructure.clients")
@Configuration
public class FeignConfig
{

}
