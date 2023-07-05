package io.busata.fourleft.endpoints.infrastructure;


import io.busata.fourleft.api.events.CommunityChallengeUpdateEvent;
import io.busata.fourleft.api.events.LeaderboardUpdated;
import io.busata.fourleft.application.wrc.WRCTickerImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementEndpoint {

    private final ApplicationEventPublisher eventPublisher;
    private  final WRCTickerImportService wrcTickerImportService;
    @PostMapping("/api/internal/management/update_leaderboard")
    public void updateLeaderboard(@RequestBody LeaderboardUpdated leaderboardUpdated) {
        eventPublisher.publishEvent(leaderboardUpdated);
    }
    @PostMapping("/api/internal/management/update_community")
    public void updateCommunity() {
        eventPublisher.publishEvent(new CommunityChallengeUpdateEvent());
    }
    @PostMapping("/api/internal/management/import_ticker")
    public void importTickerEntries() {
        this.wrcTickerImportService.importTickerEntries(true);
    }
}
