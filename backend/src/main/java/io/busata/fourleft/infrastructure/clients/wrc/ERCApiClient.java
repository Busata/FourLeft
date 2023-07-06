package io.busata.fourleft.infrastructure.clients.wrc;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="erc", url = "https://api.fiaerc.com/sdb")
public interface ERCApiClient {
    @GetMapping("/rallyevent/{rallyEventId}/contelPageId/{contentPageId}}/ticker-entries")
    TickerSummaryTo getTickerSummary(@PathVariable String rallyEventId, @PathVariable String contentPageId);
}
