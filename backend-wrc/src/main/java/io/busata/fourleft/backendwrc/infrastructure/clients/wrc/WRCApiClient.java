package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="wrc", url = "https://api.wrc.com/")
public interface WRCApiClient {
    @GetMapping("/content/result/liveUpdates?eventId=446")
    WRCLiveUpdatesTo getLiveUpdates();
}
