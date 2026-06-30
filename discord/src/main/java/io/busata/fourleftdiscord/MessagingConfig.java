package io.busata.fourleftdiscord;

import io.busata.fourleft.api.events.QueueNames;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /*
        Declare the durable queues this bot consumes so they exist on startup.
        (These were previously declared centrally by the backend; the consumer now owns them.)
     */
    @Bean
    public Queue messagesQueue() {
        return new Queue(QueueNames.MESSAGES_QUEUE, true);
    }

    @Bean
    public Queue tickerEntriesUpdateQueue() {
        return new Queue(QueueNames.TICKER_ENTRIES_UPDATE, true);
    }

}