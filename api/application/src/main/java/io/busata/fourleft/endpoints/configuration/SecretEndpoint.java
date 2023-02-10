package io.busata.fourleft.endpoints.configuration;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.importer.updaters.RacenetPlatformSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SecretEndpoint {

    private final RacenetPlatformSyncService racenetPlatformSyncService;



    @PostMapping(Routes.SECRET_SYNC_PLATFORM)
    @Async
    public void sync(@PathVariable Long participations) {
        racenetPlatformSyncService.syncPlayers(participations);
    }

}
