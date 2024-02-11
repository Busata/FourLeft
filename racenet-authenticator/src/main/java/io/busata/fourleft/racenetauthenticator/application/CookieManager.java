package io.busata.fourleft.racenetauthenticator.application;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CookieManager {

    private Map<String, String> cookies;

    public CookieManager() {
        this.cookies = new HashMap<>();
    }

    public void addCookiesFromHeader(String cookieHeader) {
        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String[] cookieParts = part.split("=", 2);
            if (cookieParts.length == 2) {
                String name = cookieParts[0].trim();
                String value = cookieParts[1].trim();
                this.cookies.put(name, value);
            }
        }
    }

    public String getCookies() {
        return this.cookies.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
    }

    public Map<String, String> getAsMap() {
        return this.cookies;
    }
}
