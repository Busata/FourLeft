package io.busata.fourleft.endpoints.dirtrally2;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleft.api.models.StageOptionTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.application.dirtrally2.QueryService;
import io.busata.fourleft.domain.dirtrally2.clubs.repository.UniquePlayersViewRepository;
import io.busata.fourleft.domain.dirtrally2.options.models.CountryConfiguration;
import io.busata.fourleft.domain.dirtrally2.options.models.CountryOption;
import io.busata.fourleft.domain.dirtrally2.options.models.StageConfiguration;
import io.busata.fourleft.domain.dirtrally2.options.models.StageOption;
import io.busata.fourleft.domain.dirtrally2.options.models.VehicleClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

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
