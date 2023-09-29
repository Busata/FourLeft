package io.busata.fourleft.endpoints.infrastructure;


import io.busata.fourleft.api.events.CommunityChallengeUpdateEvent;
import io.busata.fourleft.api.events.LeaderboardUpdated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementEndpoint {

    private final ApplicationEventPublisher eventPublisher;
    @PostMapping("/api/internal/management/update_leaderboard")
    public void updateLeaderboard(@RequestBody LeaderboardUpdated leaderboardUpdated) {
        eventPublisher.publishEvent(leaderboardUpdated);
    }
    @PostMapping("/api/internal/management/update_community")
    public void updateCommunity() {
        eventPublisher.publishEvent(new CommunityChallengeUpdateEvent());
    }

}
