package io.busata.fourleft.racenetauthenticator.application;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import io.busata.fourleft.racenetauthenticator.infrastructure.clients.dirtrally2.DR2InitialState;
import io.busata.fourleft.racenetauthenticator.infrastructure.clients.dirtrally2.DirtRally2AuthenticationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirtRally2Authentication {
    private final DirtRally2AuthenticationApi api;
    private HttpHeaders apiHeaders;

    private static final String url = "https://dirtrally2.dirtgame.com/clubs";

    @Value("${codemasters.email}")
    private String userName;

    @Value("${codemasters.pass}")
    private String password;

    private LocalDateTime lastCookieRefresh;

    public void refreshLogin() {
            if (lastCookieRefresh != null) {
                final var now = LocalDateTime.now();
                final var lastCookieTime = Duration.between(lastCookieRefresh, now).toSeconds();

                if (lastCookieTime < 300) {
                    return;
                }
            }

            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions().setHeadless(false)
                );


                Map<String, Cookie> authCookies = getAuthCookies(browser);
                apiHeaders = buildApiHeaders(authCookies);


                updateXSRFHToken();

                lastCookieRefresh = LocalDateTime.now();
                log.info("Full refresh at: {}", lastCookieRefresh);
            } catch (Exception ex) {
                log.error("Something went wrong while refreshing the login", ex);
                throw ex;
            }
    }

        private void updateXSRFHToken () {
            DR2InitialState initialState = api.getInitialState(apiHeaders);
            apiHeaders.set("RaceNet.XSRFH", initialState.identity().token());
        }

        private Map<String, Cookie> getAuthCookies (Browser browser){

            var context = browser.newContext();
            var page  = context.newPage();
            page.navigate(url);

            Locator emailField = page.locator("id=Email");
            Locator passwordField = page.locator("id=Password");
            Locator rememberMeField = page.locator("id=remember_me_container");

            Locator loginButton = page.locator("id=login_button_container").locator("button");

            emailField.fill(userName);
            passwordField.fill(password);
            rememberMeField.click();

            loginButton.click();

            page.navigate(url);

            Locator header = page.locator(".ClubsBanner__header");
            header.waitFor();


            var cookies = context.cookies();

            Map<String, Cookie> cookieMap = new HashMap<>();
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.name, cookie);
            }

            return cookieMap;
        }

        private HttpHeaders buildApiHeaders (Map < String, Cookie > cookieMap){

            HttpHeaders httpHeaders = new HttpHeaders();
            String cookieHeader = Stream.of("RaceNet", "RaceNet.XSRFC")
                    .map(cookieMap::get)
                    .map(Cookie::toString)
                    .collect(Collectors.joining("; "));

            httpHeaders.add("Cookie", cookieHeader);

            return httpHeaders;
        }

        public HttpHeaders getHeaders () {
            return apiHeaders;
        }
    }
