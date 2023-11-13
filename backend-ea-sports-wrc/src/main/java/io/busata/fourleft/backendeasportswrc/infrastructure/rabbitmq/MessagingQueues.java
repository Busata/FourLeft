package io.busata.fourleft.backendeasportswrc.infrastructure.rabbitmq;

import io.busata.fourleft.api.easportswrc.EASportsWRCQueueNames;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Component
@RequiredArgsConstructor
public class MessagingQueues {
        private final AmqpAdmin amqpAdmin;

        @SneakyThrows
        @PostConstruct
        public void createQueues() {
            for(Field f : EASportsWRCQueueNames.class.getDeclaredFields()) {
                if(Modifier.isStatic(f.getModifiers())) {
                    String value = (String) f.get(null);
                    amqpAdmin.declareQueue(new Queue(value, true));
                }
            }
        }
}