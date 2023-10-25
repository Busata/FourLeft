package io.busata.fourleft.backendeasportswrc.infrastructure.schedules;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.RacenetGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TesterSchedule {

    private final RacenetGateway gateway;


    @Scheduled(cron = "0 */1 * * * *", zone = "UTC")
    public void testRacenetApi() {
        log.info("OFFICIAL CLUBS:");
        gateway.getClubs().officialClubs().forEach(club -> {
            log.info("NAME: {} , LIKES: {}", club.clubName(), club.likeCount());
        });
    }
}
