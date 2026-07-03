package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only catalog of time-trial leaderboard combinations feeding the "Time Trials" stats tab:
 * a searchable, paged list of every {@code location + route + surface + vehicle class} tuple.
 */
@RestController
@RequiredArgsConstructor
public class TimeTrialEndpoint {

    private final TimeTrialCombinationRepository repository;

    /**
     * A page of combinations, optionally filtered by a case-insensitive substring matched against the
     * id key and the location / route / vehicle-class names.
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

        Page<TimeTrialCombination> result = repository.search(searchPattern, PageRequest.of(safePage, cappedSize));
        List<CombinationView> items = result.getContent().stream().map(CombinationView::from).toList();
        return new PageView(items, result.getTotalElements(), safePage, cappedSize, result.getTotalPages());
    }

    public record PageView(List<CombinationView> items, long total, int page, int size, int totalPages) {
    }

    public record CombinationView(String id,
                                  long locationId, String location,
                                  long routeId, String route,
                                  int surfaceCondition,
                                  long vehicleClassId, String vehicleClass,
                                  Boolean valid) {
        static CombinationView from(TimeTrialCombination t) {
            return new CombinationView(t.getId(),
                    t.getLocationId(), t.getLocation(),
                    t.getRouteId(), t.getRoute(),
                    t.getSurfaceCondition(),
                    t.getVehicleClassId(), t.getVehicleClass(),
                    t.getValid());
        }
    }
}
