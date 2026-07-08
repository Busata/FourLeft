package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.backendeasportswrc.application.timetrial.TimeTrialSyncService;
import io.busata.fourleft.backendeasportswrc.application.timetrial.TimeTrialSyncService.SyncResult;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialExportService;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialLeaderboardEntryRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialProbeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
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
    private final TimeTrialProbeRepository probeRepository;
    private final TimeTrialSyncService syncService;
    private final TimeTrialExportService exportService;

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
        // The board's real size as last probed on Racenet — distinct from {@code total}, which counts
        // only the entries we've synced (currently capped). Null when the board has never been probed.
        Integer totalEntries = probeRepository.findLatestByCombinationId(combinationId)
                .map(p -> p.getTotalEntries())
                .orElse(null);
        // When this board's current snapshot was fetched — lets the client show freshness and gate the
        // "Sync" button on the cooldown without a second round-trip. Null when never fetched.
        Instant lastFetchedAt = entryRepository.findLatestFetchedAt(combinationId).orElse(null);
        return new EntryPageView(entries, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), totalEntries, lastFetchedAt);
    }

    /**
     * Request a fresh sync of one board. Rejects unknown boards (404), a fetch already in flight (409),
     * and boards fetched inside the cooldown window (429); otherwise enqueues a {@code TT_FETCH} job and
     * returns 202. The fetch itself runs in the work queue — watch its progress on the queue status page.
     */
    @PostMapping("/api_v2/time-trials/boards/{combinationId}/sync")
    public ResponseEntity<SyncResultView> sync(@PathVariable String combinationId) {
        SyncResult result = syncService.requestManualSync(combinationId);
        SyncResultView body = SyncResultView.from(result);
        HttpStatus status = switch (result.status()) {
            case QUEUED -> HttpStatus.ACCEPTED;
            case ALREADY_RUNNING -> HttpStatus.CONFLICT;
            case TOO_SOON -> HttpStatus.TOO_MANY_REQUESTS;
            case UNKNOWN_BOARD -> HttpStatus.NOT_FOUND;
        };
        return ResponseEntity.status(status).body(body);
    }

    /**
     * The board's raw data as a CSV download — a direct, linkable file. Serves the cached export
     * (regenerated after every fetch by the {@code TT_EXPORT} job); a known board with no file yet
     * gets one built on the spot, so the link works from the moment a board has data. 404 only for
     * combinations not in the catalog.
     */
    @GetMapping(value = "/api_v2/time-trials/boards/{combinationId}/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(@PathVariable String combinationId) {
        return combinationRepository.findById(combinationId)
                .map(combination -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + downloadName(combination) + "\"")
                        .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                        .body(exportService.readOrCreateCsv(combinationId)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** A human filename for the CSV: rally, stage, surface and class instead of the raw combination id. */
    private String downloadName(TimeTrialCombination c) {
        String name = String.join("_",
                c.getLocation(), c.getRoute(),
                c.getSurfaceCondition() == 1 ? "wet" : "dry",
                c.getVehicleClass());
        // keep the header simple: strip anything outside filename-safe ascii
        return name.replaceAll("[^A-Za-z0-9._-]+", "-").replaceAll("(^-+)|(-+$)", "") + ".csv";
    }

    /** Driver autocomplete: distinct display names matching {@code q} (case-insensitive substring). */
    @GetMapping("/api_v2/time-trials/players/suggest")
    public List<String> suggest(@RequestParam String q) {
        String trimmed = q.trim();
        if (trimmed.length() < 2) {
            return List.of(); // too short to be selective; skip the scan
        }
        return entryRepository.suggestDisplayNames(trimmed, PageRequest.of(0, 10));
    }

    /** Reverse lookup: every board a player (by display name) has a stored time on, with its context. */
    // Public read-only data, deliberately open to third-party sites. Never combine with allowCredentials.
    @CrossOrigin
    @GetMapping("/api_v2/time-trials/player")
    public PlayerProfileView player(@RequestParam String name) {
        List<TimeTrialLeaderboardEntry> entries = entryRepository.findLatestByDisplayName(name);

        List<String> combinationIds = entries.stream().map(TimeTrialLeaderboardEntry::getCombinationId).distinct().toList();
        Map<String, TimeTrialCombination> combinations = combinationRepository.findAllById(combinationIds).stream()
                .collect(java.util.stream.Collectors.toMap(TimeTrialCombination::getId, c -> c));

        // Each board's real field size (last probed on Racenet), keyed by combination, for the percentile
        // column. Skips boards never probed (null total); empty input avoids an invalid SQL `IN ()`.
        Map<String, Integer> fieldSizes = combinationIds.isEmpty() ? Map.of()
                : probeRepository.findLatestByCombinationIds(combinationIds).stream()
                        .filter(p -> p.getTotalEntries() != null)
                        .collect(java.util.stream.Collectors.toMap(
                                TimeTrialProbe::getCombinationId, TimeTrialProbe::getTotalEntries, (a, b) -> a));

        List<PlayerEntryView> records = entries.stream()
                .map(e -> PlayerEntryView.from(e, combinations.get(e.getCombinationId()), fieldSizes.get(e.getCombinationId())))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator
                        .comparing(PlayerEntryView::location, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(PlayerEntryView::route, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(PlayerEntryView::surfaceCondition)
                        .thenComparing(PlayerEntryView::vehicleClass, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return new PlayerProfileView(name, records);
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

    /** {@code total} = synced entries we hold (capped); {@code totalEntries} = the board's real size on Racenet. */
    public record EntryPageView(List<EntryView> entries, int page, int size, long total, int totalPages,
                                Integer totalEntries, Instant lastFetchedAt) {
    }

    /** Outcome of a sync request — mirrors {@link SyncResult} for the client. */
    public record SyncResultView(String status, Long jobId, Instant lastFetchedAt, Instant availableAt) {
        static SyncResultView from(SyncResult r) {
            return new SyncResultView(r.status().name(), r.jobId(), r.lastFetchedAt(), r.availableAt());
        }
    }

    /**
     * A player's stored time on one board, with the board's context — a row of the profile page.
     * {@code totalEntries} is the board's real field size (last probed on Racenet), for the percentile
     * column; null when the board has never been probed.
     */
    public record PlayerEntryView(String combinationId,
                                  long locationId, String location,
                                  long routeId, String route,
                                  int surfaceCondition,
                                  long vehicleClassId, String vehicleClass,
                                  Long rank, String time, String differenceToFirst, Long platform,
                                  List<String> splits, Integer totalEntries) {
        static PlayerEntryView from(TimeTrialLeaderboardEntry e, TimeTrialCombination c, Integer totalEntries) {
            if (c == null) {
                return null; // catalog row vanished; nothing meaningful to show for it
            }
            return new PlayerEntryView(c.getId(),
                    c.getLocationId(), c.getLocation(),
                    c.getRouteId(), c.getRoute(),
                    c.getSurfaceCondition(),
                    c.getVehicleClassId(), c.getVehicleClass(),
                    e.getRank(), e.getTime(), e.getDifferenceToFirst(), e.getPlatform(), e.getSplits(), totalEntries);
        }
    }

    public record PlayerProfileView(String name, List<PlayerEntryView> entries) {
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
