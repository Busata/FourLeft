package io.busata.fourleft.infrastructure.clients.racenet;

import io.busata.fourleft.infrastructure.clients.racenet.dto.security.DR2InitialState;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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
class RacenetAuthorization {
    private final RacenetApi api;
    private WebDriver driver;
    private HttpHeaders apiHeaders;

    private static final String url = "https://dirtrally2.dirtgame.com/clubs";

    @Value("${codemasters.email}")
    private String userName;

    @Value("${codemasters.pass}")
    private String password;

    private LocalDateTime lastCookieRefresh;

    private static boolean refreshingLogin = false;

    synchronized void refreshLogin() {
        if(refreshingLogin) {
            log.info("Already refreshing, not doing it again");
            return;
        }

        refreshingLogin = true;

        try {
            if (lastCookieRefresh != null) {
                final var now = LocalDateTime.now();
                final var lastCookieTime = Duration.between(lastCookieRefresh, now).toSeconds();

                if (lastCookieTime < 300) {
                    return;
                }
            }

            log.info("Creating firefox driver");
            driver = createFirefoxDriver();

            log.info("Logging in manually, creating headers");
            Map<String, Cookie> authCookies = getAuthCookies();
            apiHeaders = buildApiHeaders(authCookies);

            log.info("Getting XSRFH token");

            updateXSRFHToken();

            lastCookieRefresh = LocalDateTime.now();
            log.info("Full refresh at: {}", lastCookieRefresh);
        }
        catch(Exception ex) {
            log.error("Something went wrong while refreshing the login", ex);
            throw ex;
        } finally {
            if(driver != null) {
                driver.manage().deleteAllCookies();
                driver.quit();
            }
            driver = null;
            refreshingLogin = false;
        }
    }


    private void updateXSRFHToken() {
        DR2InitialState initialState = api.getInitialState(apiHeaders);
        apiHeaders.set("RaceNet.XSRFH", initialState.identity().token());
    }

    private Map<String, Cookie> getAuthCookies() {
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

        log.info("Cookiemap: {}", cookieMap.size());
        HttpHeaders httpHeaders = new HttpHeaders();
        String cookieHeader = Stream.of("RaceNet", "RaceNet.XSRFC")
                .map(cookieMap::get)
                .map(Cookie::toString)
                .collect(Collectors.joining("; "));

        httpHeaders.add("Cookie", cookieHeader);
        log.info("Cookie: {}", cookieHeader);

        return httpHeaders;
    }

    private FirefoxDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxBinary binary = new FirefoxBinary();
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(binary);
        options.setHeadless(true);
        options.addArguments("--window-size=1920,1080");
        return new FirefoxDriver(options);
    }

    public HttpHeaders getHeaders() {
        return apiHeaders;
    }
}
