package io.busata.fourleft.racenetauthenticator.infrastructure.schedules;

import io.busata.fourleft.racenetauthenticator.application.EAWRCAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetAuthenticationSchedule {

    @Value("${racenet-authenticator.enable-ea-wrc:true}")
    private boolean enableEAWRC;

    private final EAWRCAuthentication eawrcAuthentication;


    @Scheduled(initialDelay = 0, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() throws URISyntaxException {
        if(enableEAWRC) {
            log.info("EA SPORTS WRC - AUTHENTICATION - ENABLED");
            eawrcAuthentication.refreshLogin();
        } else {
            log.info("EA SPORTS WRC - AUTHENTICATION - DISABLED");
        }
    }

    @Scheduled(cron = "0 */60 * * * *", zone = "UTC")
    public void updateEAWRC() throws URISyntaxException {
        if(!this.enableEAWRC){
            return;
        }
        eawrcAuthentication.refreshLogin();
    }
}
