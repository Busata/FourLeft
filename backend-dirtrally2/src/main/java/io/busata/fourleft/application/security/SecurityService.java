package io.busata.fourleft.application.security;


import io.busata.fourleft.domain.infrastructure.FourLeftRole;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityService {


    public boolean userHasRole(FourLeftRole role) {
        Authentication authentication = getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role.getRoleName()));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    public UUID getUserId() {
        String userId = ((SimpleKeycloakAccount) getAuthentication().getDetails()).getKeycloakSecurityContext().getToken().getSubject();
        return UUID.fromString(userId);
    }
}
