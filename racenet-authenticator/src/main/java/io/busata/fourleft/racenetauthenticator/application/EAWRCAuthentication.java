package io.busata.fourleft.racenetauthenticator.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v110.network.Network;
import org.openqa.selenium.devtools.v110.network.model.ResponseReceived;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class EAWRCAuthentication {

    private static final String url = "https://racenet.com";
    private final String tokenUrl = "https://web-api.racenet.com/api/identity/auth";


    @Value("${codemasters.email}")
    private String userName;

    @Value("${code-file}")
    private String codeFilePath;

    @Value("${codemasters.pass}")
    private String password;

    private final ObjectMapper objectMapper;

    private EAWRCToken token;

    public void refreshLogin() {
        WebDriverManager manager = WebDriverManager.chromedriver();

        ChromeOptions capabilities = new ChromeOptions();

        capabilities.addArguments("--disable-dev-shm-usage", "--remote-allow-origins=*","--window-size=1920,1080","--headless");
        manager.capabilities(capabilities);

        ChromeDriver driver = (ChromeDriver) manager.create();

        DevTools devTools = driver.getDevTools();
        devTools.createSessionIfThereIsNotOne();

        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.responseReceived(), (ResponseReceived event) -> {
            try {
                if (event.getResponse().getUrl().equals(tokenUrl)) {
                    Network.GetResponseBodyResponse send = devTools.send(Network.getResponseBody(event.getRequestId()));

                    try {
                        token = objectMapper.readValue(send.getBody(), EAWRCToken.class);
                    } catch (JsonProcessingException e) {
                        log.error("Something went wrong reading the EA WRC token.");
                    }
                }
            } catch(Exception ex) {
                //Evil but perhaps hides the crashing
            }
        });

        try {
            triggerAuthCall(driver);
        } catch (Exception ex) {
            log.error("Something went wrong while refreshing the login", ex);
            throw ex;
        } finally {
            manager.quit();
        }
    }

    @SneakyThrows
    private void triggerAuthCall(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));

        driver.get(url);

        WebElement signinButton = driver.findElement(By.cssSelector("button[data-test-id=\"signInBtn\""));

        signinButton.click();

        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("logInBtn"));

        emailField.sendKeys(userName);
        passwordField.sendKeys(password);
        loginButton.click();

        if(this.buttonExists(driver, By.id("btnSendCode"))) {
            driver.findElement(By.id("btnSendCode")).click();

            log.info("Waiting until {} appears", codeFilePath);

            while(!Files.exists(Path.of(codeFilePath))) {
                Thread.sleep(1000);
            }

            String s = Files.readString(Path.of(codeFilePath));


            driver.findElement(By.id("twoFactorCode")).sendKeys(s);
            driver.findElement(By.id("btnSubmit")).click();
        }

        WebElement eaWrcButton = driver.findElement(By.cssSelector("a[href=\"/ea_sports_wrc\""));
        eaWrcButton.click();



    }


    public boolean buttonExists(WebDriver driver, By id) {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
        boolean exists = !driver.findElements(id).isEmpty();
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        return exists;
    }

    public EAWRCToken getHeaders() {
        return token;
    }
}

