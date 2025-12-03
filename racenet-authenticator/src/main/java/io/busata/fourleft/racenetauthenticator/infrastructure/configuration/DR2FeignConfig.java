package io.busata.fourleft.racenetauthenticator.infrastructure.configuration;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
public class DR2FeignConfig {
    @Bean
    public Client feignClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts(), new java.security.SecureRandom());

        return new Client.Default(
                sslContext.getSocketFactory(),
                (hostname, session) -> true  // Disable hostname verification
        );
    }

    private TrustManager[] trustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
    }
}
