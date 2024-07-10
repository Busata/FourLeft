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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClubsImporterService {

    private final DiscordGateway discordGateway;
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


        List<ClubImportProcess> failedClubs = runningProcesses.values().stream().filter(process -> process.getState() == ProcessState.FAILED).toList();

        try {
            failedClubs.forEach(process -> {
                log.error("Process failed, disabling sync for club {}", process.getClubId());
                clubConfigurationService.setClubSync(process.getClubId(), false);
            });
        } catch (Exception ex) {
            log.error("Could not disable clubs!");
        }
        discordGateway.createMessage(1173372471207018576L, new SimpleDiscordMessageTo("Disabled clubs count: %s.".formatted(failedClubs.size()), List.of()));

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
