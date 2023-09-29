package io.busata.fourleft.infrastructure.clients.wrc;


import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="wrc", url = "https://api.wrc.com/")
public interface WRCApiClient {
    @GetMapping("/content/result/liveUpdates?eventId=363")
    WRCLiveUpdatesTo getLiveUpdates();
}
