package io.busata.fourleft.racenetauthenticator.application;


import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RacenetAuthentication {
    private static final String url = "https://racenet.com";
    private LocalDateTime lastCookieRefresh;
    private BrowserUpProxy proxy;

    private HttpHeaders apiHeaders;

    @Value("${codemasters.email}")
    private String userName;

    @Value("${codemasters.pass}")
    private String password;


    public void refreshLogin() {
        WebDriverManager wdm = WebDriverManager.chromedriver();

        proxy = new BrowserUpProxyServer();
        proxy.start(0);

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        ChromeOptions capabilities = new ChromeOptions();
        capabilities.addArguments("--ignore-ssl-errors=yes");
        capabilities.addArguments("--ignore-certificate-errors");
        capabilities.setProxy(seleniumProxy);
        wdm.capabilities(capabilities);

        WebDriver driver = wdm.create();


        try {
            if (lastCookieRefresh != null) {
                final var now = LocalDateTime.now();
                final var lastCookieTime = Duration.between(lastCookieRefresh, now).toSeconds();

                if (lastCookieTime < 300) {
                    return;
                }
            }


            proxy.addRequestFilter((request, content, messageInfo) -> {
                Optional.ofNullable(request.headers().get("authorization")).ifPresent(header -> {
                    log.info(header);
                });
                return null;
            });


            getLoginStuff(driver);

            lastCookieRefresh = LocalDateTime.now();
            log.info("Full refresh at: {}", lastCookieRefresh);
        } catch (Exception ex) {
            log.error("Something went wrong while refreshing the login", ex);
            throw ex;
        } finally {
            driver.manage().deleteAllCookies();
            proxy.abort();
            driver.quit();
        }
    }


    @SneakyThrows
    private void getLoginStuff(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

        driver.get(url);

        WebElement signInButton = driver.findElement(By.cssSelector("button[data-test-id=\"signInBtn\"]"));
        signInButton.click();

        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));

        WebElement loginButton = driver.findElement(By.id("logInBtn"));

        emailField.sendKeys(userName);
        passwordField.sendKeys(password);


        loginButton.click();

        WebElement dirtRally = driver.findElement(By.cssSelector("a[href=\"/dirtRally2\""));

        dirtRally.click();


        driver.findElement(By.id("root"));


    }
}
