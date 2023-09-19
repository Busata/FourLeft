package io.busata.fourleft.racenetauthenticator.infrastructure.clients.dirtrally2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="racenetauthenticationapi", url="https://dirtrally2.dirtgame.com/")
public interface DirtRally2AuthenticationApi {
    @GetMapping(value = "api/ClientStore/GetInitialState", produces = MediaType.APPLICATION_JSON_VALUE)
    DR2InitialState getInitialState(@RequestHeader HttpHeaders headers);
}
