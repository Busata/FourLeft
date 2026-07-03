package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialLeaderboardEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The user-facing time-trial board browser: a drill-down catalog (rally → stage → surface → class) of
 * the boards that actually have fetched entries, and a server-paged view of one board's leaderboard.
 * Separate from {@link TimeTrialEndpoint} (the probe-coverage/ops view over the full ~9.5k catalog):
 * this one only surfaces boards with data and pages the entries, which can run to tens of thousands.
 */
@RestController
@RequiredArgsConstructor
public class TimeTrialBoardEndpoint {

    private static final int MAX_PAGE_SIZE = 200;

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialLeaderboardEntryRepository entryRepository;

    /** The drill-down tree, built only from combinations that currently have stored entries. */
    @GetMapping("/api_v2/time-trials/catalog")
    public CatalogView catalog() {
        List<String> withData = entryRepository.findDistinctCombinationIds();
        List<TimeTrialCombination> combinations = new ArrayList<>(combinationRepository.findAllById(withData));
        combinations.sort(Comparator
                .comparing(TimeTrialCombination::getLocation, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TimeTrialCombination::getRoute, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(TimeTrialCombination::getSurfaceCondition)
                .thenComparing(TimeTrialCombination::getVehicleClass, Comparator.nullsLast(Comparator.naturalOrder())));

        // location -> route -> surface -> classes, preserving the sorted order via LinkedHashMaps.
        Map<Long, RallyBuilder> rallies = new LinkedHashMap<>();
        for (TimeTrialCombination c : combinations) {
            rallies.computeIfAbsent(c.getLocationId(), k -> new RallyBuilder(c.getLocationId(), c.getLocation()))
                    .stage(c.getRouteId(), c.getRoute())
                    .surface(c.getSurfaceCondition())
                    .add(new ClassView(c.getVehicleClassId(), c.getVehicleClass(), c.getId()));
        }

        return new CatalogView(rallies.values().stream().map(RallyBuilder::build).toList());
    }

    /** One page of a board's leaderboard, ranked ascending. */
    @GetMapping("/api_v2/time-trials/boards/{combinationId}/entries")
    public EntryPageView entries(@PathVariable String combinationId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "50") int size) {
        int clamped = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamped, Sort.by("rank").ascending());
        Page<TimeTrialLeaderboardEntry> result = entryRepository.findLatestPage(combinationId, pageable);
        List<EntryView> entries = result.getContent().stream().map(EntryView::from).toList();
        return new EntryPageView(entries, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    // --- view models -------------------------------------------------------

    public record CatalogView(List<RallyView> rallies) {
    }

    public record RallyView(long locationId, String location, List<StageView> stages) {
    }

    public record StageView(long routeId, String route, List<SurfaceView> surfaces) {
    }

    /** {@code surfaceCondition}: 0 = dry, 1 = wet. Each surface lists the car classes with a board. */
    public record SurfaceView(int surfaceCondition, List<ClassView> classes) {
    }

    /** A leaf of the tree — carries the combination id so the client can request its entries directly. */
    public record ClassView(long vehicleClassId, String vehicleClass, String combinationId) {
    }

    public record EntryView(Long rank, String displayName, Long nationalityId, String vehicle,
                            Long platform, String time, String differenceToFirst, String timePenalty,
                            List<String> splits) {
        static EntryView from(TimeTrialLeaderboardEntry e) {
            return new EntryView(e.getRank(), e.getDisplayName(), e.getNationalityID(), e.getVehicle(),
                    e.getPlatform(), e.getTime(), e.getDifferenceToFirst(), e.getTimePenalty(), e.getSplits());
        }
    }

    public record EntryPageView(List<EntryView> entries, int page, int size, long total, int totalPages) {
    }

    // --- tree assembly helpers ---------------------------------------------

    private static final class RallyBuilder {
        private final long locationId;
        private final String location;
        private final Map<Long, StageBuilder> stages = new LinkedHashMap<>();

        RallyBuilder(long locationId, String location) {
            this.locationId = locationId;
            this.location = location;
        }

        StageBuilder stage(long routeId, String route) {
            return stages.computeIfAbsent(routeId, k -> new StageBuilder(routeId, route));
        }

        RallyView build() {
            return new RallyView(locationId, location, stages.values().stream().map(StageBuilder::build).toList());
        }
    }

    private static final class StageBuilder {
        private final long routeId;
        private final String route;
        private final Map<Integer, List<ClassView>> surfaces = new LinkedHashMap<>();

        StageBuilder(long routeId, String route) {
            this.routeId = routeId;
            this.route = route;
        }

        List<ClassView> surface(int surfaceCondition) {
            return surfaces.computeIfAbsent(surfaceCondition, k -> new ArrayList<>());
        }

        StageView build() {
            List<SurfaceView> surfaceViews = surfaces.entrySet().stream()
                    .map(e -> new SurfaceView(e.getKey(), e.getValue()))
                    .toList();
            return new StageView(routeId, route, surfaceViews);
        }
    }
}
