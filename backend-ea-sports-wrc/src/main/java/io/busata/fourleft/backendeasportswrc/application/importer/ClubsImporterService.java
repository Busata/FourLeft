package io.busata.fourleft.backendeasportswrc.application.importer;

import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ProcessState;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClubsImporterService {

    private final ClubConfigurationService clubConfigurationService;

    private final Map<String, ClubImportProcess> runningProcesses = new HashMap<>();

    private final List<ClubImportProcessHandler> processHandlers;

    public int sync() {
        initiateClubProcessing();
        orchestrateRunningProcesses();

        return runningProcesses.size();
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
        //Control the currently running ones.
        runningProcesses.values().forEach(process -> {
            do {
                control(process);
            } while (process.canContinue());
        });

        //Error handling
        runningProcesses.values().stream().filter(process -> process.getState() == ProcessState.FAILED).forEach(process -> {
            log.error("Process failed, disabling sync for club {}", process.getClubId());
            clubConfigurationService.setClubSync(process.getClubId(), false);
        });

        //Remove processes that are done.
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
