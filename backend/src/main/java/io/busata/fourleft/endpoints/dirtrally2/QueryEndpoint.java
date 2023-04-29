package io.busata.fourleft.endpoints.dirtrally2;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.application.dirtrally2.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Collections.reverseOrder;

@RestController
@RequiredArgsConstructor
@Slf4j
public class QueryEndpoint
{
    private final QueryService queryService;

    @GetMapping(Routes.QUERY_VEHICLE_CLASS)
    public List<VehicleTo> getVehiclesForClass(@PathVariable String vehicleClass) {
        return queryService.getVehiclesForClass(vehicleClass);
    }

    @GetMapping(Routes.QUERY_NAME)
    public List<String> queryName(@RequestParam String query) {
       return queryService.queryName(query);
    }

    @GetMapping(Routes.QUERY_TRACK)
    public QueryTrackResultsTo queryTrack(@RequestParam String stageName) {
        return queryService.queryTrack(stageName);
    }
}
