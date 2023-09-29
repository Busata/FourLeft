package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;

public record WRCLiveUpdateContentsTo(
        String value,
        String type,
        WRCLiveUpdateMediaTo media

) {
}
