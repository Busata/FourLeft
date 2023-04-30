package io.busata.fourleft.infrastructure.configuration;

import io.busata.fourleft.api.events.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Component
@RequiredArgsConstructor
public class MessagingQueues {
        private final AmqpAdmin amqpAdmin;

        @SneakyThrows
        @PostConstruct
        public void createQueues() {
            for(Field f : QueueNames.class.getDeclaredFields()) {
                if(Modifier.isStatic(f.getModifiers())) {
                    String value = (String) f.get(null);
                    amqpAdmin.declareQueue(new Queue(value, true));
                }
            }
        }
}
