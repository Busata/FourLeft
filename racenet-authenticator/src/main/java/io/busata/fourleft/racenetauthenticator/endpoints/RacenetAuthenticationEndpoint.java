package io.busata.fourleft.racenetauthenticator.endpoints;


import io.busata.fourleft.racenetauthenticator.application.DirtRally2Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping
@RequiredArgsConstructor
public class RacenetAuthenticationEndpoint {

    private final DirtRally2Authentication authentication;

    @GetMapping("/api/external/authentication/dirtrally2")
    public Map<String, String> getDirtRally2Headers() {
        return authentication.getHeaders().toSingleValueMap();
    }
}
