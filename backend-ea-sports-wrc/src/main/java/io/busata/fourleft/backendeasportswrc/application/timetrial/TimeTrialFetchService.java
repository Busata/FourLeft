package io.busata.fourleft.backendeasportswrc.application.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialProbe;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialLeaderboardEntryRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialProbeRepository;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial.FetchResult;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial.TimeTrialBoardGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches one time-trial board in full and stores it: stream every page from the paging gateway,
 * replacing the board's stored rows with the fresh snapshot and appending a {@link TimeTrialProbe}
 * observation recording the total and how many entries changed since the previous fetch (churn).
 *
 * <p>Built for boards of any size, including the few with tens of thousands of entries. Racenet caps
 * the page at 20, so a big board is thousands of <em>sequential</em> calls (minutes of wall-clock).
 * To survive that:
 * <ul>
 *   <li>each page is saved in its own short transaction, so peak memory is one page (no board-wide
 *       list, no growing persistence context) and the DB connection is released between pages while
 *       the next call waits on the rate limiter;</li>
 *   <li>{@code heartbeat} is pinged after each page so the queue's stale-job reclaimer doesn't mistake
 *       a legitimately-slow fetch for a crashed worker and run it again in parallel.</li>
 * </ul>
 *
 * <p>The snapshot is swapped, not cleared-then-rebuilt: the new rows are inserted under this run's
 * {@code fetchedAt} and only once they've all landed does the previous generation get deleted. So the
 * old snapshot stays live throughout — a crash mid-fetch leaves the previous board (and the churn
 * baseline it provides) intact rather than a blank or half-written one, and no probe row is written,
 * so the next run simply redoes it against the preserved baseline. The request rate is governed
 * globally by resilience4j on {@code TimeTrialLeaderboardClient} (instance {@code racenet-timetrial}).
 *
 * <p>During a fetch the board transiently holds two generations; a future reader should take the rows
 * with the latest {@code fetchedAt} per combination. After any successful fetch only one remains.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrialFetchService {

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialLeaderboardEntryRepository entryRepository;
    private final TimeTrialProbeRepository probeRepository;
    private final TimeTrialBoardGateway boardGateway;

    /**
     * @param heartbeat pinged after each stored page to keep the job's stale clock fresh; pass a no-op
     *                  when calling outside the work queue
     */
    public FetchReport fetchCombination(String combinationId, Runnable heartbeat) {
        TimeTrialCombination combination = combinationRepository.findById(combinationId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown time-trial combination: " + combinationId));

        // Snapshot of the current board as (player key -> time) for churn, loaded lean (two columns,
        // not full entities). Left in place — the new generation is inserted alongside it and only
        // swapped in once complete, so a crash preserves this baseline.
        Map<String, String> previousTimes = loadPreviousTimes(combinationId);

        Instant fetchedAt = Instant.now();
        int[] totalAndChanged = {0, 0};

        FetchResult result = boardGateway.fetch(combination.getLocationId(), combination.getRouteId(),
                combination.getSurfaceCondition(), combination.getVehicleClassId(),
                page -> {
                    persistPage(combinationId, page, previousTimes, fetchedAt, totalAndChanged);
                    heartbeat.run();
                });

        if (!result.boardExists()) {
            // Board gone since it was last probed: clear it out entirely and record the absence.
            entryRepository.deleteByCombinationId(combinationId);
            probeRepository.save(new TimeTrialProbe(combinationId, false, null, null));
            log.info("Time-trial fetch • {} • board no longer exists", combinationId);
            return FetchReport.missing();
        }

        // The new generation is fully in — swap it in by dropping the previous one (and any partial
        // rows an earlier crashed run left, which also predate this fetch).
        entryRepository.deleteSupersededBy(combinationId, fetchedAt);

        int total = totalAndChanged[0];
        int changed = totalAndChanged[1];
        probeRepository.save(new TimeTrialProbe(combinationId, true, total, changed));
        log.info("Time-trial fetch • {} • {} entries stored ({} changed, Racenet reported {})",
                combinationId, total, changed, result.totalEntries());
        return new FetchReport(true, total, changed);
    }

    /** Persist one page in its own transaction (via the repository) and tally churn against the previous snapshot. */
    private void persistPage(String combinationId, List<TimeTrialLeaderboardEntryTo> page,
                             Map<String, String> previousTimes, Instant fetchedAt, int[] totalAndChanged) {
        List<TimeTrialLeaderboardEntry> rows = new ArrayList<>(page.size());
        for (TimeTrialLeaderboardEntryTo entry : page) {
            String previous = previousTimes.get(playerKey(entry));
            if (previous == null || !previous.equals(timeOf(entry))) {
                totalAndChanged[1]++;
            }
            totalAndChanged[0]++;
            rows.add(toEntity(combinationId, entry, fetchedAt));
        }
        entryRepository.saveAll(rows);
    }

    private Map<String, String> loadPreviousTimes(String combinationId) {
        List<Object[]> rows = entryRepository.findPlayerTimes(combinationId);
        Map<String, String> byPlayer = new HashMap<>(Math.max(16, rows.size() * 2));
        for (Object[] row : rows) {
            byPlayer.put((String) row[0], row[1] == null ? "" : (String) row[1]);
        }
        return byPlayer;
    }

    private static String timeOf(TimeTrialLeaderboardEntryTo entry) {
        return Optional.ofNullable(entry.time()).orElse("");
    }

    private static String playerKey(TimeTrialLeaderboardEntryTo entry) {
        return Optional.ofNullable(entry.ssid())
                .or(() -> Optional.ofNullable(entry.wrcPlayerId()))
                .orElse(entry.displayName());
    }

    private static TimeTrialLeaderboardEntry toEntity(String combinationId, TimeTrialLeaderboardEntryTo entry, Instant fetchedAt) {
        return TimeTrialLeaderboardEntry.builder()
                .combinationId(combinationId)
                .ssid(entry.ssid())
                .displayName(entry.displayName())
                .wrcPlayerId(entry.wrcPlayerId())
                .rank(entry.rank())
                .nationalityID(entry.nationalityID())
                .platform(entry.platform())
                .vehicle(entry.vehicle())
                .time(entry.time())
                .differenceToFirst(entry.differenceToFirst())
                .timePenalty(entry.timePenalty())
                .splits(entry.splits())
                .fetchedAt(fetchedAt)
                .build();
    }
}
