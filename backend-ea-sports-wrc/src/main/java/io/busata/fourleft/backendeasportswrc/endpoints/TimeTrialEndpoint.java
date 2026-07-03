package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialProbeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-only catalog of time-trial leaderboard combinations feeding the "Time Trials" stats tab:
 * a searchable, paged list of every {@code location + route + surface + vehicle class} tuple, each
 * enriched with its latest probe (does the board exist + entry count).
 */
@RestController
@RequiredArgsConstructor
public class TimeTrialEndpoint {

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialProbeRepository probeRepository;

    /**
     * A page of combinations, optionally filtered by a case-insensitive substring matched against the
     * id key and the location / route / vehicle-class names, each merged with its latest probe.
     *
     * @param search optional search term (matches "Monte", "Bollène", "WRC", "6-99-0-19", …)
     * @param page   zero-based page index
     * @param size   page size (capped at 200)
     */
    @GetMapping("/api_v2/time-trials")
    public PageView list(@RequestParam(required = false) String search,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "50") int size) {
        String term = (search == null) ? "" : search.trim();
        String searchPattern = "%" + term + "%"; // empty term -> "%%" matches everything
        int cappedSize = Math.min(Math.max(size, 1), 200);
        int safePage = Math.max(page, 0);

        Page<TimeTrialCombination> result =
                combinationRepository.search(searchPattern, PageRequest.of(safePage, cappedSize));

        List<String> ids = result.getContent().stream().map(TimeTrialCombination::getId).toList();
        Map<String, TimeTrialProbe> latestProbe = ids.isEmpty() ? Map.of()
                : probeRepository.findLatestForCombinations(ids).stream()
                        .collect(Collectors.toMap(TimeTrialProbe::getCombinationId, Function.identity()));

        List<CombinationView> items = result.getContent().stream()
                .map(c -> CombinationView.from(c, latestProbe.get(c.getId())))
                .toList();
        return new PageView(items, result.getTotalElements(), safePage, cappedSize, result.getTotalPages());
    }

    public record PageView(List<CombinationView> items, long total, int page, int size, int totalPages) {
    }

    /**
     * A combination plus its latest probe. {@code valid}/{@code totalEntries}/{@code probedAt} are
     * null when the combination has never been probed.
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
