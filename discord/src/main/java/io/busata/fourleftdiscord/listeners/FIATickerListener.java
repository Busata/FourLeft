package io.busata.fourleftdiscord.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.events.QueueNames;
import io.busata.fourleft.api.events.FIATickerUpdateEvent;
import io.busata.fourleft.api.models.FIATickerUpdateTo;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FIATickerListener {

    private final DiscordMessageGateway facade;
    private final DR2FieldMapper fieldMapper;
    @RabbitListener(queues = QueueNames.TICKER_ENTRIES_UPDATE)
    public void listen(FIATickerUpdateEvent event) {
        try {
            event.updates().stream().sorted(Comparator.comparing(FIATickerUpdateTo::dateTime)).forEach(tickerUpdateTo -> {
                EmbedCreateSpec.Builder tickerUpdate = EmbedCreateSpec.builder()
                        .title(fieldMapper.createEmoticon(tickerUpdateTo.tickerEventKey()) + " • " + tickerUpdateTo.title() + " • <t:%s:R>".formatted(tickerUpdateTo.dateTime()))
                        .color(fieldMapper.createColour(tickerUpdateTo.tickerEventKey()))
                        .description(tickerUpdateTo.text().replace("\\n", "\n").replace("\\u200B", "\u200B"));

                Optional.ofNullable(tickerUpdateTo.imageUrl()).ifPresent(tickerUpdate::image);
                facade.postMessage(Snowflake.of(892372267445661727L),
                        tickerUpdate.build(),
                        MessageType.RESULTS_POST
                );
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
