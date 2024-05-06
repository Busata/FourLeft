package io.busata.fourleft.racenetauthenticator.endpoints;


import io.busata.fourleft.racenetauthenticator.application.DirtRally2Authentication;
import io.busata.fourleft.racenetauthenticator.application.EAWRCAuthentication;
import io.busata.fourleft.racenetauthenticator.application.EAWRCToken;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RacenetAuthenticationEndpoint {

    private final DirtRally2Authentication authentication;
    private final EAWRCAuthentication eawrcAuthentication;

    @GetMapping("/api/external/authentication/dirtrally2")
    public Map<String, String> getDirtRally2Headers() {
        var headers = authentication.getHeaders();
        if(headers != null) {
            return headers.toSingleValueMap();
        }        
        else  {
            return null;
        }
    }

    @GetMapping("/api/external/authentication/easportswrc")
    public EAWRCToken getEAWRCToken() {
        return eawrcAuthentication.getHeaders();
    }
}
