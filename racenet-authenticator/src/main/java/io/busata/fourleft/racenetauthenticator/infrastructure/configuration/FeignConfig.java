package io.busata.fourleft.racenetauthenticator.infrastructure.configuration;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("io.busata.fourleft.racenetauthenticator.infrastructure.clients")
@Configuration
public class FeignConfig
{

}
