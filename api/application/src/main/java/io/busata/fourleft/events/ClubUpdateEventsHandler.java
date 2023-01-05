package io.busata.fourleft.events;


import io.busata.fourleft.api.messages.ClubUpdated;
import io.busata.fourleft.api.messages.QueueNames;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class ClubUpdateEventsHandler {

    private final RabbitTemplate rabbitMQ;


    @EventListener
    public void handle(ClubUpdated clubUpdated) {

        rabbitMQ.convertAndSend(QueueNames.CLUB_EVENT_QUEUE, clubUpdated);
    }

}
