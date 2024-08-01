package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

//https://api.wrc.com/content/result/liveUpdates?eventId=449
//https://api.fiaerc.com/content/result/liveUpdates?eventId=445

@FeignClient(name="wrc", url = "https://api.wrc.com")
public interface WRCApiClient {
    @GetMapping("/content/result/liveUpdates?eventId=455")
    WRCLiveUpdatesTo getLiveUpdates();
}
