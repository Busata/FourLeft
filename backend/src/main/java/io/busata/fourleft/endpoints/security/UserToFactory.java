package io.busata.fourleft.endpoints.security;

import io.busata.fourleft.api.models.security.UserTo;
import io.busata.fourleft.infrastructure.common.Factory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@Factory
public class UserToFactory {

    public UserTo create(Authentication authentication) {
        String name = authentication.getName();
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_",""))
                .collect(Collectors.toList());

        return new UserTo(name, roles);
    }
}
