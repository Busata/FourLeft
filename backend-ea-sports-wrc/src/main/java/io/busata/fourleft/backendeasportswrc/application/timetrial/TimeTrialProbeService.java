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
import java.util.Objects;

/**
 * Probes every time-trial board for one rally: hit Racenet per combination, record whether the board
 * exists and its entry count as append-only {@link TimeTrialProbe} history. Existence/counts are
 * never written back onto the catalog — the catalog stays immutable and the latest probe row is the
 * current state.
 *
 * <p>Boards are probed sequentially; the request rate is governed by resilience4j on
 * {@code TimeTrialLeaderboardClient} (instance {@code racenet-timetrial}), which caps the rate
 * globally across all concurrent probe jobs — no pacing logic here. A single board's fetch failure
 * is logged and skipped; if <em>every</em> board fails (auth down, rate-limit exhausted, API broken)
 * the whole pass fails so the operator sees it rather than a silent "0 boards".
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrialProbeService {

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialProbeRepository probeRepository;
    private final TimeTrialBoardGateway boardGateway;

    public ProbeReport probeLocation(long locationId) {
        List<TimeTrialCombination> combinations = combinationRepository.findByLocationId(locationId);
        if (combinations.isEmpty()) {
            log.warn("Time-trial probe • location {} • no combinations in catalog", locationId);
            return new ProbeReport(0, 0, 0);
        }

        List<TimeTrialProbe> observations = new ArrayList<>();
        int failures = 0;
        RuntimeException lastFailure = null;

        for (TimeTrialCombination combination : combinations) {
            try {
                ProbeResult result = boardGateway.probe(combination.getLocationId(), combination.getRouteId(),
                        combination.getSurfaceCondition(), combination.getVehicleClassId());
                observations.add(new TimeTrialProbe(combination.getId(), result.boardExists(),
                        result.boardExists() ? result.totalEntries() : null));
            } catch (RuntimeException ex) {
                failures++;
                lastFailure = ex;
                log.error("Time-trial probe • {} • failed", combination.getId(), ex);
            }
        }

        // Nothing recorded and everything failed -> surface it as a failed job, don't pretend success.
        if (observations.isEmpty() && failures > 0) {
            throw lastFailure;
        }

        probeRepository.saveAll(observations);

        int boardsFound = (int) observations.stream().filter(TimeTrialProbe::isBoardExists).count();
        int totalEntries = observations.stream()
                .map(TimeTrialProbe::getTotalEntries)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        log.info("Time-trial probe • location {} • {} probed, {} boards exist, {} total entries ({} failures)",
                locationId, observations.size(), boardsFound, totalEntries, failures);
        return new ProbeReport(observations.size(), boardsFound, totalEntries);
    }
}
