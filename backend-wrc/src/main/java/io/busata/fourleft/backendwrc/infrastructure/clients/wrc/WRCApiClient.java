package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="wrc", url = "https://api.wrc.com")
public interface WRCApiClient {
    @GetMapping("/content/result/liveUpdates")
    WRCLiveUpdatesTo getLiveUpdates(@RequestParam("eventId") String eventId);


    @GetMapping("/content/filters/calendar?championship=wrc")
    WRCCalendarTo getCalendar(@RequestParam("year") String year);

}
