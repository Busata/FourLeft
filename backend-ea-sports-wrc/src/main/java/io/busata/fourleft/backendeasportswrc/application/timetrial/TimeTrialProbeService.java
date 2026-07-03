package io.busata.fourleft.backendeasportswrc.application.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialProbeRepository;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial.ProbeResult;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial.TimeTrialBoardGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Probes every time-trial board for one rally: hit Racenet per combination, record whether the board
 * exists and its entry count as append-only {@link TimeTrialProbe} history. Existence/counts are
 * never written back onto the catalog — the catalog stays immutable and the latest probe row is the
 * current state.
 *
 * <p>Runs one virtual thread per board, capped by a {@link Semaphore} so Racenet isn't hammered. A
 * single board's fetch failure is logged and skipped; if <em>every</em> board fails (e.g. the API
 * isn't wired, or auth is down) the whole pass fails so the operator sees it rather than a silent
 * "0 boards".
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrialProbeService {

    /** Concurrent Racenet probes in flight for one rally. Gentle by default. */
    private static final int MAX_CONCURRENT_PROBES = 6;

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialProbeRepository probeRepository;
    private final TimeTrialBoardGateway boardGateway;

    public ProbeReport probeLocation(long locationId) {
        List<TimeTrialCombination> combinations = combinationRepository.findByLocationId(locationId);
        if (combinations.isEmpty()) {
            log.warn("Time-trial probe • location {} • no combinations in catalog", locationId);
            return new ProbeReport(0, 0, 0);
        }

        Semaphore limiter = new Semaphore(MAX_CONCURRENT_PROBES);
        List<TimeTrialProbe> observations = new ArrayList<>();
        int[] failures = {0};
        RuntimeException[] lastFailure = {null};

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = combinations.stream()
                    .map(combination -> Map.entry(combination, executor.submit(() -> {
                        limiter.acquire();
                        try {
                            return boardGateway.probe(combination.getLocationId(), combination.getRouteId(),
                                    combination.getSurfaceCondition(), combination.getVehicleClassId());
                        } finally {
                            limiter.release();
                        }
                    })))
                    .toList();

            for (var future : futures) {
                TimeTrialCombination combination = future.getKey();
                try {
                    ProbeResult result = future.getValue().get();
                    observations.add(new TimeTrialProbe(combination.getId(), result.boardExists(),
                            result.boardExists() ? result.totalEntries() : null));
                } catch (Exception ex) {
                    failures[0]++;
                    lastFailure[0] = (ex.getCause() instanceof RuntimeException re) ? re : new RuntimeException(ex);
                    log.error("Time-trial probe • {} • failed", combination.getId(), ex);
                }
            }
        }

        // Nothing recorded and everything failed -> surface it as a failed job, don't pretend success.
        if (observations.isEmpty() && failures[0] > 0) {
            throw lastFailure[0];
        }

        probeRepository.saveAll(observations);

        int boardsFound = (int) observations.stream().filter(TimeTrialProbe::isBoardExists).count();
        int totalEntries = observations.stream()
                .map(TimeTrialProbe::getTotalEntries)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        log.info("Time-trial probe • location {} • {} probed, {} boards exist, {} total entries ({} failures)",
                locationId, observations.size(), boardsFound, totalEntries, failures[0]);
        return new ProbeReport(observations.size(), boardsFound, totalEntries);
    }
}
