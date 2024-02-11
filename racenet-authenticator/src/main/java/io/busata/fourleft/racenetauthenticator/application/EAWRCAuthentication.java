package io.busata.fourleft.racenetauthenticator.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class EAWRCAuthentication {

    @Value("${codemasters.email}")
    private String userName;

    @Value("${codemasters.pass}")
    private String password;

    private final ObjectMapper objectMapper;

    private EAWRCToken token;

    @SneakyThrows
    public void refreshLogin() throws URISyntaxException {
        WebClient client = WebClient.builder().build();
        CookieManager cookieManager = new CookieManager();

        //INITIAL URL THAT RETURNS FID
        URI initiatorURI = new URI("https://accounts.ea.com/connect/auth?client_id=RACENET_1_JS_WEB_APP&response_type=code&redirect_uri=https://racenet.com/oauthCallback");
        ResponseEntity<String> initiatorResponse = client.get()
                .uri(initiatorURI)
                .retrieve()
                .toEntity(String.class)
                .block();

        if (initiatorResponse.getStatusCode().value() != 302) {
            throw new RuntimeException("Expected 302 from initiator URL");
        }

        String loginURIWithFID = initiatorResponse.getHeaders().getFirst("Location");
        var queryParams = getQueryParameters(loginURIWithFID);
        String fid = queryParams.get("fid");

        //SECOND URL, ALSO RETURNS EXECUTION(?)
        ResponseEntity<String> loginURIResponse = client.get()
                .uri(new URI(loginURIWithFID))
                .retrieve()
                .toEntity(String.class)
                .block();

        loginURIResponse.getHeaders().get("set-cookie")
                .forEach(cookieManager::addCookiesFromHeader);


        if (loginURIResponse.getStatusCode().value() != 302) {
            throw new RuntimeException("Expected 302 from loginURI");
        }

        String loginURIwithFIDAndExecution = loginURIResponse.getHeaders().getFirst("Location");

        //DO LOGIN CALL WITH USER AND PASS
        String actualLoginURI = "https://signin.ea.com" + loginURIwithFIDAndExecution;

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", this.userName);
        formData.add("password", this.password);
        formData.add("_eventId", "submit");
        formData.add("showAgeUp", "true");
        formData.add("loginMethod", "emailPassword");
        formData.add("thirdPartyCaptchaResponse", "");
        formData.add("_rememberMe", "on");
        formData.add("rememberMe", "on");

        ResponseEntity<String> block = client.post()
                .uri(new URI(actualLoginURI))
                .header("Cookie", cookieManager.getCookies())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .toEntity(String.class)
                .block();

        if (block.getStatusCode().value() != 200) {
            throw new RuntimeException("Not correct status yet, no point in continue");
        }


        URI next = new URI("https://accounts.ea.com/connect/auth?response_type=code&redirect_uri=https%3A%2F%2Fracenet.com%2FoauthCallback&client_id=RACENET_1_JS_WEB_APP&fid=" + fid);
        ResponseEntity<String> getCode = client.get()
                .uri(next)
                .retrieve()
                .toEntity(String.class)
                .block();
        String codeRedirect = getCode.getHeaders().getFirst("Location");

        String s = getQueryParameters(codeRedirect).get("code");

        URI finalURL = new URI("https://web-api.racenet.com/api/identity/auth");

        String response = client.post()
                .uri(finalURL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new EAWRCRequestToken(
                        s,
                        "RACENET_1_JS_WEB_APP",
                        "",
                        "authorization_code",
                        "https://racenet.com/oauthCallback",
                        ""
                )).retrieve()
                .bodyToMono(String.class)
                .block();

        String jsonString = objectMapper.readValue(response, String.class);
        EAWRCToken eawrcToken = objectMapper.readValue(jsonString, EAWRCToken.class);

        log.info("Token updated.");

       this.token = eawrcToken;

    }


    private Map<String, String> getQueryParameters(String url) {

        final var queryParameters = url.split("\\?")[1];

        return Arrays.stream(queryParameters.split("&")).collect(Collectors.toMap(
                (value) -> value.split("=")[0],
                (value) -> value.split("=")[1],
                (existing, newValue) -> existing
        ));
    }

    public EAWRCToken getHeaders() {
        return token;
    }
}

