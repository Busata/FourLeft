package io.busata.fourleft.backendacrally.infrastructure.security;

import io.busata.fourleft.backendacrally.domain.models.agent.ApiKey;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.agent.ApiKeyService;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Authenticates the agent's ingestion + races + issue calls by {@code Authorization: Bearer <api_key>}.
 * Acts on {@code /acrally-api/sessions/**} (ingestion), {@code /acrally-api/agent/races/**} (the Races
 * tab) and {@code /acrally-api/agent/issues} (problem reports); a valid, non-revoked key belonging to
 * an active user sets an {@link AgentPrincipal}. Anything
 * else is left unauthenticated so the authorization layer 401s. Note: {@code /agent/pair/**} is NOT
 * covered here — pairing is browser (session) driven, so the prefix is the narrower {@code races}.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final List<String> PATH_PREFIXES =
            List.of("/acrally-api/sessions", "/acrally-api/agent/races", "/acrally-api/agent/issues");
    private static final String BEARER = "Bearer ";

    private final ApiKeyService apiKeyService;
    private final AppUserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PATH_PREFIXES.stream().noneMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            String token = header.substring(BEARER.length()).trim();
            Optional<ApiKey> key = apiKeyService.authenticate(token);
            if (key.isPresent()) {
                AppUser user = userRepository.findById(key.get().getUserId()).orElse(null);
                if (user != null && !user.isBanned()) {
                    AgentPrincipal principal = new AgentPrincipal(user.getId(), key.get().getId());
                    var authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_AGENT")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
