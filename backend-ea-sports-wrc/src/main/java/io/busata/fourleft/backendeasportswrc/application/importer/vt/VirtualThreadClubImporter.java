package io.busata.fourleft.backendeasportswrc.application.importer.vt;

import io.busata.fourleft.backendeasportswrc.application.importer.ClubImporter;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.infrastructure.properties.ImporterProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The club importer: each club is imported by one <i>virtual thread</i> running a plain, linear,
 * blocking flow ({@link ClubImportWorker#importClub}). Racenet fetches block the virtual thread
 * cheaply; there is no explicit state enum to advance.
 *
 * <p><b>This class owns only orchestration:</b> the virtual-thread executor lifecycle and the
 * in-flight guard so the 5s schedule never starts a second worker for a club that is still running.
 * All the real import logic lives in {@link ClubImportWorker}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualThreadClubImporter implements ClubImporter {

    private final ClubConfigurationService clubConfigurationService;
    private final ClubImportWorker worker;
    private final ImporterProperties importerProperties;

    /** One virtual thread per submitted club; created lazily per task, so this is effectively unbounded. */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Clubs currently being imported. A club is removed only when its worker thread finishes (success or failure). */
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    @Override
    public int sync() {
        // Only ever top up to the configured ceiling. Workers free their slot when they finish, so a
        // later tick picks up the clubs skipped here — instead of firing off every syncable club at once.
        int availableSlots = importerProperties.getMaxConcurrentClubs() - inFlight.size();
        if (availableSlots <= 0) {
            return inFlight.size();
        }

        clubConfigurationService.findSyncableClubs()
                .stream()
                .map(ClubConfiguration::getClubId)
                .filter(clubId -> !inFlight.contains(clubId))
                .limit(availableSlots)
                .forEach(this::startIfNotRunning);

        return inFlight.size();
    }

    private void startIfNotRunning(String clubId) {
        // Set.add returns false if the club is already in flight -> skip.
        if (!inFlight.add(clubId)) {
            return;
        }
        executor.submit(() -> {
            try {
                worker.importClub(clubId);
            } catch (Exception ex) {
                // Worker is expected to handle its own domain failures (e.g. disabling sync). This is the
                // last-resort net so one club's crash never leaks the in-flight slot.
                log.error("Club {} • unhandled importer failure", clubId, ex);
            } finally {
                inFlight.remove(clubId);
            }
        });
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
