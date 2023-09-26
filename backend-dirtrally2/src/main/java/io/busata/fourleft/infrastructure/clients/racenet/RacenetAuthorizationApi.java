package io.busata.fourleft.infrastructure.clients.racenet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(name="racenetauthorization", url="${racenetauthentication.url}", configuration = FeignConfiguration.class)
public interface RacenetAuthorizationApi {


    @RequestMapping("api/external/authentication/dirtrally2")
    public Map<String, String> getHeaders();
}
