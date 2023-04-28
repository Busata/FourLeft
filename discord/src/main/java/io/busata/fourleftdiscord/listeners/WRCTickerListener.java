package io.busata.fourleftdiscord.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import io.busata.fourleft.api.messages.QueueNames;
import io.busata.fourleft.api.models.WRCTickerUpdateTo;
import io.busata.fourleft.api.messages.MessageType;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WRCTickerListener {

    private final DiscordMessageGateway facade;
    private final DR2FieldMapper fieldMapper;
    @RabbitListener(queues = QueueNames.TICKER_ENTRIES_UPDATE)
    public void listen(List<WRCTickerUpdateTo> update) {
        try {
            update.stream().sorted(Comparator.comparing(WRCTickerUpdateTo::dateTime)).forEach(wrcTickerUpdateTo -> {
                EmbedCreateSpec.Builder tickerUpdate = EmbedCreateSpec.builder()
                        .title(fieldMapper.createEmoticon(wrcTickerUpdateTo.tickerEventKey()) + " • " + wrcTickerUpdateTo.title() + " • <t:%s:R>".formatted(wrcTickerUpdateTo.dateTime()))
                        .color(fieldMapper.createColour(wrcTickerUpdateTo.tickerEventKey()))
                        .description(wrcTickerUpdateTo.text().replace("\\n", "\n").replace("\\u200B", "\u200B"));

                Optional.ofNullable(wrcTickerUpdateTo.imageUrl()).ifPresent(tickerUpdate::image);
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
