package io.busata.wrcserver.importer.client;

import io.busata.wrcserver.importer.client.models.WRCSeasonDataTo;
import io.busata.wrcserver.importer.client.models.WRCTickerSummaryTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="wrc", url="https://api.wrc.com")
public interface WRCApi {


    @GetMapping("contel-page/{contelId}/calendar/season/{season}/competition/{competition}")
    WRCSeasonDataTo getSeasonData(@PathVariable String contelId, @PathVariable String season, @PathVariable String competition);


    @GetMapping("sdb/rallyevent/{eventId}/contelPageId/739206/ticker-entries")
    WRCTickerSummaryTo getTickerEntries(@PathVariable String eventId);
}
