package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;

import java.util.List;

public record WRCLiveUpdatesTo(

        List<WRCLiveUpdateItemTo> items
) {
}
