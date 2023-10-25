package io.busata.fourleft.backendeasportswrc.infrastructure.clients.authorization;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient(name="racenetauthorization", url="${racenetauthentication.url}")
public interface RacenetAuthorizationApi {

    @RequestMapping("api/external/authentication/easportswrc")
    EAWRCToken getToken();
}
