package io.busata.fourleft.racenetauthenticator.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

    // Run with a visible browser by overriding this (e.g. in the "override" profile) to debug the flow.
    @Value("${racenet-authenticator.headless:true}")
    private boolean headless;

    // How long elementExists() waits for an optional step (consent / 2FA) to render before
    // deciding it's absent. The login submit redirects through OAuth, so these pages don't
    // appear instantly; too short a wait skips the step (e.g. the "send code" 2FA page).
    @Value("${racenet-authenticator.element-wait-ms:5000}")
    private double elementWaitMs;

    private final ObjectMapper objectMapper;

    private EAWRCToken token;

    public void refreshLogin() {
        // Suppress Playwright's first-run auto-download of the full browser set
        // (Chromium + Firefox + WebKit). Only Chromium is installed/used; see the
        // Dockerfile and the `playwright-install-chromium` exec goal in pom.xml.
        try (Playwright playwright = Playwright.create(
                new Playwright.CreateOptions().setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")))) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(headless)
                            .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"))
            );

            try {
                BrowserContext context = browser.newContext(
                        new Browser.NewContextOptions().setViewportSize(1920, 1080)
                );
                Page page = context.newPage();
                page.setDefaultTimeout(60_000);

                // Intercept the token response, mirroring the previous DevTools network listener.
                page.onResponse(response -> {
                    if (response.url().equals(tokenUrl)) {
                        try {
                            token = objectMapper.readValue(response.text(), EAWRCToken.class);
                        } catch (Exception e) {
                            log.error("Something went wrong reading the EA WRC token.", e);
                        }
                    }
                });

                triggerAuthCall(page);
                log.info("done");
            } finally {
                browser.close();
            }
        } catch (Exception ex) {
            log.error("Something went wrong while refreshing the login", ex);
            throw ex;
        }
    }

    @SneakyThrows
    private void triggerAuthCall(Page page) {
        page.navigate(url);

        Locator signinButton = page.locator("xpath=//button[normalize-space()=\"SIGN IN\"]");
        signinButton.click();

        Locator emailField = page.locator("#email");
        emailField.waitFor();

        // The visible control is <label for="rememberMe">, which overlays the checkbox and
        // intercepts pointer events, so a direct click on the input times out. Click the label
        // (like a user would), and only when not already checked so we never toggle it off.
        if (elementExists(page, "#rememberMe") && !page.locator("#rememberMe").isChecked()) {
            page.locator("label[for=\"rememberMe\"]").click();
        }

        //Racenet switched to a different login flow at some point
        //First had to enter e-mail , then press "next", then enter password.
        //Maybe A-B testing, so supporting both.
        if (!elementExists(page, "#password")) {
            log.info("Password on next flow");
            emailField.fill(userName);
            page.locator("#logInBtn").click();

            Locator passwordField = page.locator("#password");
            passwordField.fill(password);
            page.locator("#logInBtn").click();
        } else {
            log.info("Both fields flow");
            emailField.fill(userName);
            page.locator("#password").fill(password);
            page.locator("#logInBtn").click();
        }

        // check if there is a label with the for attribute of "readAccept"
        if (elementExists(page, "label[for=\"readAccept\"]")) {
            page.locator("label[for=\"readAccept\"]").click();
            page.locator("#btnNext").click();
        }

        if (elementExists(page, "#btnSendCode")) {
            page.locator("#btnSendCode").click();

            log.info("Waiting until {} appears", codeFilePath);

            while (!Files.exists(Path.of(codeFilePath))) {
                Thread.sleep(1000);
            }

            String s = Files.readString(Path.of(codeFilePath));

            log.info("Code found: {}", s);

            page.locator("#twoFactorCode").fill(s);
            page.locator("#btnSubmit").click();
            log.info("Code entered and submitted");
        }

        Locator eaWrcButton = page.locator("a[href=\"/ea_sports_wrc\"]");
        eaWrcButton.click();
        log.info("Clicked on EA WRC link");
    }

    /**
     * Waits up to {@code elementWaitMs} for the element to become visible, returning true as
     * soon as it appears or false if it never does. A bounded wait (rather than an immediate
     * probe) is required because the optional consent / 2FA steps render after OAuth redirects
     * that complete some time after the action that triggered them.
     */
    public boolean elementExists(Page page, String selector) {
        try {
            page.locator(selector).first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(elementWaitMs));
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    public EAWRCToken getHeaders() {
        return token;
    }
}
