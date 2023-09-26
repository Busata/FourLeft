package io.busata.fourleft.racenetauthenticator.infrastructure.schedules;

import io.busata.fourleft.racenetauthenticator.application.DirtRally2Authentication;
import io.busata.fourleft.racenetauthenticator.application.RacenetAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RacenetAuthenticationSchedule {

    private final DirtRally2Authentication dirtRally2Authentication;
    private final RacenetAuthentication racenetAuthentication;

    @Scheduled(initialDelay = 0, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() {
        //dirtRally2Authentication.refreshLogin();
        racenetAuthentication.refreshLogin();
    }


    @Scheduled(cron = "0 */5 * * * *", zone = "UTC")
    public void updateDirtyRally2() {
        //dirtRally2Authentication.refreshLogin();
    }
}
