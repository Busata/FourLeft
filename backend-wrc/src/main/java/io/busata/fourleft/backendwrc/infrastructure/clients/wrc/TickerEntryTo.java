package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;

import java.util.Optional;

public record TickerEntryTo(
        String id,
        String datetimeUnix,
        TickerEventTo tickerEvent,
        String title,
        String text,
        String externalStageId,
        boolean pinned,
        Optional<TickerEntryImageTo> tickerEntryImage

) {
}
