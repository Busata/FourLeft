package io.busata.fourleft.backendeasportswrc.application.importer;

import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ProcessState;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubsImporterService {

    /** Safety net so a hung Racenet fetch can't block the worker thread forever. */
    private static final long FETCH_TIMEOUT_SECONDS = 180;

    private final DiscordGateway discordGateway;
    private final ClubConfigurationService clubConfigurationService;

    private final Map<String, ClubImportProcess> runningProcesses = new HashMap<>();

    private final List<ClubImportProcessHandler> processHandlers;

    public int sync() {
        initiateClubProcessing();
        orchestrateRunningProcesses();

        return runningProcesses.size();
    }

    /**
     * Import a single club to completion, reusing the existing state-machine handlers.
     * Entry point for the job-queue worker (see application.work.queue): the queue
     * owns scheduling/lifecycle, this owns the actual per-club import logic.
     *
     * @throws ClubImportFailedException if the club ends in the FAILED state
     */
    public void runClub(String clubId) {
        ClubImportProcess process = new ClubImportProcess(clubId);
        try {
            // The state machine is tick-based: the FETCHING_* states are yield points
            // (keepProcessing=false) where async fetches are in flight. The legacy
            // importer re-enters control() on the next scheduler tick, giving those
            // futures time to complete. Here we run to completion in one call, so at a
            // yield point we block on the in-flight futures before pumping the next
            // transition instead of exiting (which would cancel them via complete()).
            do {
                control(process);
                if (!process.isDone() && !process.canContinue()) {
                    awaitInFlight(clubId, process);
                }
            } while (!process.isDone());

            if (process.getState() == ProcessState.FAILED) {
                throw new ClubImportFailedException(clubId);
            }
        } finally {
            process.complete();
        }
    }

    /** Wait for the async fetches the current state is blocked on before continuing. */
    private void awaitInFlight(String clubId, ClubImportProcess process) {
        List<CompletableFuture<?>> inFlight = new ArrayList<>();
        if (process.getDetails() != null) {
            inFlight.add(process.getDetails());
        }
        if (process.getStandings() != null) {
            inFlight.addAll(process.getStandings());
        }
        if (process.getLeaderboards() != null) {
            inFlight.addAll(process.getLeaderboards());
        }
        if (inFlight.isEmpty()) {
            return;
        }
        try {
            CompletableFuture.allOf(inFlight.toArray(CompletableFuture[]::new))
                    .get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ClubImportFailedException(clubId);
        } catch (TimeoutException ex) {
            log.error("Club {} • fetch timed out after {}s, failing import", clubId, FETCH_TIMEOUT_SECONDS);
            throw new ClubImportFailedException(clubId);
        } catch (ExecutionException ex) {
            // Importers convert their own errors into *Failed results; let the next
            // control() observe them. Nothing to do here.
        }
    }

    private void initiateClubProcessing() {
        clubConfigurationService.findSyncableClubs()
                .stream()
                .map(ClubConfiguration::getClubId)
                .forEach(clubId -> {
                    if (runningProcesses.containsKey(clubId)) {
                        return;
                    }
                    runningProcesses.put(clubId, new ClubImportProcess(clubId));
                });
    }

    private void orchestrateRunningProcesses() {
        // Control the currently running ones.
        runningProcesses.values().forEach(process -> {
            do {
                control(process);
            } while (process.canContinue());
        });

        List<ClubImportProcess> failedClubs = runningProcesses.values().stream()
                .filter(process -> process.getState() == ProcessState.FAILED).toList();

        try {
            failedClubs.forEach(process -> {
                log.error("Process failed, disabling sync for club {}", process.getClubId());
                clubConfigurationService.setClubSync(process.getClubId(), false);
            });
        } catch (Exception ex) {
            log.error("Could not disable clubs!");
        }
        if (failedClubs.size() > 0) {
            discordGateway.createMessage(1173372471207018576L,
                    new SimpleDiscordMessageTo("Disabled clubs count: %s.".formatted(failedClubs.size()), List.of()));
        }
        // Remove processes that are done.
        runningProcesses.values().stream().filter(ClubImportProcess::isDone).forEach(ClubImportProcess::complete);
        runningProcesses.values().removeIf(ClubImportProcess::isDone);
    }

    private void control(ClubImportProcess process) {
        processHandlers.stream()
                .filter(handler -> handler.canHandle(process))
                .findFirst()
                .ifPresent(handler -> {
                    handler.control(process);
                });
    }

}
