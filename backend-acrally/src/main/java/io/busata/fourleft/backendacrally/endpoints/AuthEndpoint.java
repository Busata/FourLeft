package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.AuthUserTo;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Session bookkeeping for the browser. Signing in happens exclusively through the Steam
 * flow ({@link SteamAuthEndpoint}); this endpoint only reports and ends sessions.
 */
@RestController
@RequestMapping("/acrally-api/auth")
@RequiredArgsConstructor
public class AuthEndpoint {

    private final AppUserRepository users;

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/me")
    public AuthUserTo me(@AuthenticationPrincipal AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        // Read through to the DB rather than echoing the session principal: the display
        // name is user-editable, and the session snapshot goes stale after a rename.
        return users.findById(principal.getId())
                .map(user -> new AuthUserTo(user.getId(), user.getDisplayName(), user.getStatus().name(), user.isAdmin()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
