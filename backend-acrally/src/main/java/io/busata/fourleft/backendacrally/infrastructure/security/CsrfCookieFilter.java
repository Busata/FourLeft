package io.busata.fourleft.backendacrally.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Renders the {@link CsrfToken} so {@code CookieCsrfTokenRepository} writes the
 * XSRF-TOKEN cookie eagerly. Without this the token is deferred and the SPA has no
 * cookie to echo on its first mutating request. (Spring Security reference pattern.)
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Accessing the token value triggers the repository to persist the cookie.
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
