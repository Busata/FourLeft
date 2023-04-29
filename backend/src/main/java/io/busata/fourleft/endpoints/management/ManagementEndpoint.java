package io.busata.fourleft.endpoints.management;


import io.busata.fourleft.api.messages.LeaderboardUpdated;
import io.busata.fourleft.api.messages.QueueNames;
import io.busata.fourleft.application.wrc.WRCTickerImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementEndpoint {

    private final RabbitTemplate rabbitMQ;
    private  final WRCTickerImportService wrcTickerImportService;
    @PostMapping("/api/management/update_leaderboard")
    public void updateLeaderboard(@RequestBody LeaderboardUpdated leaderboardUpdated) {
        rabbitMQ.convertAndSend(QueueNames.LEADERBOARD_UPDATE, leaderboardUpdated);
    }
    @PostMapping("/api/management/import_ticker")
    public void importTickerEntries() {
        this.wrcTickerImportService.importTickerEntries(true);
    }
}
