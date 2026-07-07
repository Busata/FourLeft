package io.busata.fourleft.backendacrally.infrastructure.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        // Session-backed context: the login endpoint saves the authentication here and
        // the browser carries it via the HttpOnly session cookie on subsequent requests.
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository,
                                           ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        // Plain (non-XOR) handler so the XSRF-TOKEN cookie value equals the X-XSRF-TOKEN
        // header Angular's HttpClient echoes back — the standard SPA CSRF setup.
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                        // Agent endpoints are not cookie-authenticated (device_code / bearer key),
                        // so CSRF — a cookie-session defense — doesn't apply to them.
                        .ignoringRequestMatchers(
                                "/acrally-api/agent/pair/start",
                                "/acrally-api/agent/pair/token",
                                "/acrally-api/sessions/**",
                                "/acrally-api/agent/races/**"))
                // Force the CSRF token to materialize so the cookie is written on every
                // response (incl. the initial GET the SPA makes before any mutation).
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                // Bearer (API key) authentication for the agent ingestion endpoints.
                .addFilterBefore(apiKeyAuthFilter, AuthorizationFilter.class)
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                // We create sessions lazily (on login), never eagerly.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Let Boot's internal ERROR dispatch reach /error and render the status the
                        // controller already set. Without this, a 404 from a bearer-authed request is
                        // re-authorized unauthenticated on the error dispatch and masked as 401.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/acrally-api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/acrally-api/auth/register", "/acrally-api/auth/login").permitAll()
                        // Agent device-pairing handshake: no session, authenticated by the device_code itself.
                        .requestMatchers(HttpMethod.POST, "/acrally-api/agent/pair/start", "/acrally-api/agent/pair/token").permitAll()
                        // Administration surface: only ROLE_ADMIN principals. A non-admin session hits 403.
                        .requestMatchers("/acrally-api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // REST semantics: unauthenticated access to a guarded route is 401, not a redirect.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }
}
