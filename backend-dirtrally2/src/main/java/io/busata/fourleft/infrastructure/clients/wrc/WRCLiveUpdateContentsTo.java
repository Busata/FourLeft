package io.busata.fourleft.infrastructure.clients.wrc;

public record WRCLiveUpdateContentsTo(
        String value,
        String type,
        WRCLiveUpdateMediaTo media

) {
}
