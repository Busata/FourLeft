package io.busata.fourleft.endpoints.infrastructure;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.security.UserTo;
import io.busata.fourleft.application.security.UserToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class SecurityEndpoint {
    private final UserToFactory userToFactory;

    @GetMapping(Routes.SECURITY_USER)
    public UserTo getUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(userToFactory::create)
                .orElseThrow();

    }
}
