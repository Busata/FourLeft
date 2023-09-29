package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;

import java.util.List;

public record WRCLiveUpdateItemTo(
        String type,
        String title,
        String id,
        String eventTime,
        List<WRCLiveUpdateContentsTo> contents,
        WRCLiveUpdateCategoryTo category

) {
}
