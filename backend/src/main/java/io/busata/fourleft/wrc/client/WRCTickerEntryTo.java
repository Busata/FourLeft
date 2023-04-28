package io.busata.fourleft.wrc.client;

import java.util.Optional;

public record WRCTickerEntryTo(
        String id,
        String datetimeUnix,
        WRCTickerEventTo tickerEvent,
        String title,
        String text,
        String externalStageId,
        boolean pinned,
        Optional<WRCTickerEntryImageTo> tickerEntryImage

) {
}
