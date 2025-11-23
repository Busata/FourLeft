package io.busata.fourleft.racenetauthenticator.infrastructure.schedules;

import io.busata.fourleft.racenetauthenticator.application.DirtRally2Authentication;
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

    @Value("${racenet-authenticator.enable-dirt-rally2:true}")
    private boolean enableDirtRally2;
    @Value("${racenet-authenticator.enable-ea-wrc:true}")
    private boolean enableEAWRC;

    private final DirtRally2Authentication dirtRally2Authentication;
    private final EAWRCAuthentication eawrcAuthentication;


    @Scheduled(initialDelay = 0, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() throws URISyntaxException {
        if(enableDirtRally2) {
            log.info("DIRT RALLY 2 - AUTHENTICATION - ENABLED");
            try {
                dirtRally2Authentication.refreshLogin();
            } catch (Exception ex) {
                log.error("Something went wrong trying to refresh DR2.0");
            }
        } else {
            log.info("DIRT RALLY 2 - AUTHENTICATION - DISABLED");
        }

        if(enableEAWRC) {
            log.info("EA SPORTS WRC - AUTHENTICATION - ENABLED");
            eawrcAuthentication.refreshLogin();
        } else {
            log.info("EA SPORTS WRC - AUTHENTICATION - DISABLED");
        }
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "UTC")
    public void updateDirtyRally2() {
        if(!this.enableDirtRally2){
            return;
        }
        dirtRally2Authentication.refreshLogin();
    }

    @Scheduled(cron = "0 */60 * * * *", zone = "UTC")
    public void updateEAWRC() throws URISyntaxException {
        if(!this.enableEAWRC){
            return;
        }
        eawrcAuthentication.refreshLogin();
    }
}
