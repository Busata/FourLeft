package io.busata.fourleft.infrastructure.clients.wrc;

import java.util.List;

public record WRCLiveUpdatesTo(

        List<WRCLiveUpdateItemTo> items
) {
}
