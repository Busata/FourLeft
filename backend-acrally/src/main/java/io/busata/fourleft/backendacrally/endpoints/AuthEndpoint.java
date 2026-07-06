package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.AuthUserTo;
import io.busata.fourleft.api.acrally.models.LoginRequestTo;
import io.busata.fourleft.api.acrally.models.RegisterRequestTo;
import io.busata.fourleft.backendacrally.domain.models.identity.IdentityProvider;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.identity.LinkedIdentityRepository;
import io.busata.fourleft.backendacrally.domain.services.identity.SteamProfileService;
import io.busata.fourleft.backendacrally.domain.services.user.UserRegistrationService;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/acrally-api/auth")
@RequiredArgsConstructor
public class AuthEndpoint {

    private final UserRegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final LinkedIdentityRepository linkedIdentityRepository;
    private final SteamProfileService steamProfileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserTo register(@RequestBody RegisterRequestTo request,
                               HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AppUser user = registrationService.register(request.email(), request.password(), request.displayName());
        // Establish the session immediately so the user lands logged in after signup.
        authenticate(user.getEmail(), request.password(), httpRequest, httpResponse);
        return toAuthUser(user);
    }

    @PostMapping("/login")
    public AuthUserTo login(@RequestBody LoginRequestTo request,
                            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AppUserDetails principal = authenticate(request.email(), request.password(), httpRequest, httpResponse);
        // Best-effort, off the request thread: keep the Steam snapshot fresh on each sign-in.
        linkedIdentityRepository.findByUserIdAndProvider(principal.getId(), IdentityProvider.STEAM)
                .ifPresent(identity -> steamProfileService.refreshAsync(identity.getProviderUserId()));
        return toAuthUser(principal);
    }

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
        return toAuthUser(principal);
    }

    private AppUserDetails authenticate(String email, String password,
                                        HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return (AppUserDetails) authentication.getPrincipal();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void onAuthenticationFailure() {
        // Bad credentials / locked account -> 401, no detail (don't reveal which failed).
    }

    private AuthUserTo toAuthUser(AppUser user) {
        return new AuthUserTo(user.getId(), user.getEmail(), user.getDisplayName(), user.getStatus().name());
    }

    private AuthUserTo toAuthUser(AppUserDetails principal) {
        return new AuthUserTo(principal.getId(), principal.getEmail(), principal.getDisplayName(), principal.getStatus());
    }
}
