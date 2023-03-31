package io.busata.wrcserver.importer.client.models;

import java.util.Optional;

public record WRCTickerEntryTo(
        String id,
        Long datetimeUnix,

        WRCTickerEventTo tickerEvent,
        String title,
        String text,
        String externalStageId,
        boolean pinned,
        Optional<WRCTickerEntryImageTo> tickerEntryImage
) {
}
