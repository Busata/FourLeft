package io.busata.wrcserver;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagingQueues {
        private final AmqpAdmin amqpAdmin;

        @SneakyThrows
        @PostConstruct
        public void createQueues() {
            amqpAdmin.declareQueue(new Queue("q.wrc.ticker", true));
        }
}
