package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialProbeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-only catalog of time-trial leaderboard combinations feeding the "Time Trials" stats tab.
 * Returns every combination (a fixed ~9.5k rows) merged with its latest probe (does the board exist,
 * entry counts, when). Small and bounded, so search / sort / paging all happen client-side.
 */
@RestController
@RequiredArgsConstructor
public class TimeTrialEndpoint {

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialProbeRepository probeRepository;

    @GetMapping("/api_v2/time-trials")
    public List<CombinationView> list() {
        Map<String, TimeTrialProbe> latestProbe = probeRepository.findAllLatest().stream()
                .collect(Collectors.toMap(TimeTrialProbe::getCombinationId, Function.identity()));

        return combinationRepository
                .findAll(Sort.by("locationId", "routeId", "surfaceCondition", "vehicleClassId"))
                .stream()
                .map(c -> CombinationView.from(c, latestProbe.get(c.getId())))
                .toList();
    }

    /**
     * A combination plus its latest probe. {@code valid}/{@code totalEntries}/{@code changedEntries}/
     * {@code probedAt} are null when the combination has never been probed.
     */
    public record CombinationView(String id,
                                  long locationId, String location,
                                  long routeId, String route,
                                  int surfaceCondition,
                                  long vehicleClassId, String vehicleClass,
                                  Boolean valid, Integer totalEntries, Integer changedEntries, Instant probedAt) {
        static CombinationView from(TimeTrialCombination c, TimeTrialProbe probe) {
            Boolean valid = (probe == null) ? null : probe.isBoardExists();
            Integer totalEntries = (probe == null) ? null : probe.getTotalEntries();
            Integer changedEntries = (probe == null) ? null : probe.getChangedEntries();
            Instant probedAt = (probe == null) ? null : probe.getProbedAt();
            return new CombinationView(c.getId(),
                    c.getLocationId(), c.getLocation(),
                    c.getRouteId(), c.getRoute(),
                    c.getSurfaceCondition(),
                    c.getVehicleClassId(), c.getVehicleClass(),
                    valid, totalEntries, changedEntries, probedAt);
        }
    }
}
