package io.busata.fourleft.racenetauthenticator.endpoints;


import io.busata.fourleft.racenetauthenticator.application.EAWRCAuthentication;
import io.busata.fourleft.racenetauthenticator.application.EAWRCToken;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RacenetAuthenticationEndpoint {

    private final EAWRCAuthentication eawrcAuthentication;

    @GetMapping("/api/external/authentication/easportswrc")
    public EAWRCToken getEAWRCToken() {
        return eawrcAuthentication.getHeaders();
    }
}
