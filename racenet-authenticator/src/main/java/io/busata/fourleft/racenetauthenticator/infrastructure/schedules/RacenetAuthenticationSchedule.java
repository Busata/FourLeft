package io.busata.fourleft.racenetauthenticator.infrastructure.schedules;

import io.busata.fourleft.racenetauthenticator.application.DirtRally2Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RacenetAuthenticationSchedule {

    private final DirtRally2Authentication dirtRally2Authentication;

    @Scheduled(initialDelay = 0, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() {
        dirtRally2Authentication.refreshLogin();
    }


    @Scheduled(cron = "0 */5 * * * *", zone = "UTC")
    public void updateDirtyRally2() {
        dirtRally2Authentication.refreshLogin();
    }
}
