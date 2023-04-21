package io.busata.fourleft.endpoints.management;


import io.busata.fourleft.api.messages.LeaderboardUpdated;
import io.busata.fourleft.api.messages.QueueNames;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementEndpoint {

    private final RabbitTemplate rabbitMQ;
    @PostMapping("/api/management/update_leaderboard")
    public void updateLeaderboard(@RequestBody LeaderboardUpdated leaderboardUpdated) {
        rabbitMQ.convertAndSend(QueueNames.LEADERBOARD_UPDATE, leaderboardUpdated);
    }
}