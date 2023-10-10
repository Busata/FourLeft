package io.busata.fourleft.infrastructure.schedules;

import io.busata.fourleft.application.discord.DiscordMemberUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiscordUpdateSchedule {

    private final DiscordMemberUpdater updater;

    @Scheduled(initialDelay = 15000, fixedDelay=Long.MAX_VALUE)
    public void updateAfterStartup() {
        log.info("Startup update of discord server members");
        updater.sync();
    }
}
