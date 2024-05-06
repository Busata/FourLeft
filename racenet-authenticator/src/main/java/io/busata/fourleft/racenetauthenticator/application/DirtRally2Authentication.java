package io.busata.fourleft.racenetauthenticator.application;

import io.busata.fourleft.racenetauthenticator.infrastructure.clients.dirtrally2.DR2InitialState;
import io.busata.fourleft.racenetauthenticator.infrastructure.clients.dirtrally2.DirtRally2AuthenticationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    @Value("${selenium.url}")
    private String seleniumUrl;

    private LocalDateTime lastCookieRefresh;

    public void refreshLogin() {
        WebDriver driver = createDriver();

        try {
            if (lastCookieRefresh != null) {
                final var now = LocalDateTime.now();
                final var lastCookieTime = Duration.between(lastCookieRefresh, now).toSeconds();

                if (lastCookieTime < 300) {
                    return;
                }
            }


            Map<String, Cookie> authCookies = getAuthCookies(driver);
            apiHeaders = buildApiHeaders(authCookies);


            updateXSRFHToken();

            lastCookieRefresh = LocalDateTime.now();
            log.info("Full refresh at: {}", lastCookieRefresh);
        }
        catch(Exception ex) {
            log.error("Something went wrong while refreshing the login", ex);
            throw ex;
        } finally {
            driver.manage().deleteAllCookies();
            driver.quit();
        }
    }

    private WebDriver createDriver() {
        WebDriver driver;
        try {
            var options = new FirefoxOptions();
            driver = new RemoteWebDriver(new URL(this.seleniumUrl), new FirefoxOptions(), false);
            
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return driver;
    }


    private void updateXSRFHToken() {
        DR2InitialState initialState = api.getInitialState(apiHeaders);
        apiHeaders.set("RaceNet.XSRFH", initialState.identity().token());
    }

    private Map<String, Cookie> getAuthCookies(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

        driver.get(url);

        WebElement emailField = driver.findElement(By.id("Email"));
        WebElement passwordField = driver.findElement(By.id("Password"));
        WebElement rememberMeField = driver.findElement(By.id("remember_me_container"));

        WebElement loginButton = driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/form/div[4]/div/input"));

        emailField.sendKeys(userName);
        passwordField.sendKeys(password);
        rememberMeField.click();

        loginButton.click();

        driver.get(url);

        driver.findElement(By.xpath("/html/body/div[1]/div[1]/div/main/div[1]/div/h3"));


        Set<Cookie> cookies = driver.manage().getCookies();

        Map<String, Cookie> cookieMap = new HashMap<>();
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie);
        }

        return cookieMap;
    }

    private HttpHeaders buildApiHeaders(Map<String,Cookie> cookieMap) {

        HttpHeaders httpHeaders = new HttpHeaders();
        String cookieHeader = Stream.of("RaceNet", "RaceNet.XSRFC")
                .map(cookieMap::get)
                .map(Cookie::toString)
                .collect(Collectors.joining("; "));

        httpHeaders.add("Cookie", cookieHeader);

        return httpHeaders;
    }

    public HttpHeaders getHeaders() {
        return apiHeaders;
    }
}
