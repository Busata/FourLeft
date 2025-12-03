package io.busata.fourleft.racenetauthenticator.infrastructure.clients.dirtrally2;

import io.busata.fourleft.racenetauthenticator.infrastructure.configuration.DR2FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="racenetauthenticationapi", url="https://dirtrally2.dirtgame.com/", configuration= DR2FeignConfig.class)
public interface DirtRally2AuthenticationApi {
    @GetMapping(value = "api/ClientStore/GetInitialState", produces = MediaType.APPLICATION_JSON_VALUE)
    DR2InitialState getInitialState(@RequestHeader HttpHeaders headers);
}
