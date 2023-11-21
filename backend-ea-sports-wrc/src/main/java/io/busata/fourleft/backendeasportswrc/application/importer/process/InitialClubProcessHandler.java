package io.busata.fourleft.backendeasportswrc.application.importer.process;

import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ProcessState;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitialClubProcessHandler implements ClubImportProcessHandler {

    private final ClubService clubService;

    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.START, this::processStart
    );

    private void processStart(ClubImportProcess process) {
        if (this.clubService.exists(process.getClubId())) {
            if (this.clubService.requiresDetailUpdate(process.getClubId())) {
                process.setState(ProcessState.UPDATE_EXISTING_CLUB);
            } else if (this.clubService.requiresLeaderboardUpdate(process.getClubId())){
                process.setState(ProcessState.CHECK_LEADERBOARDS);
            } else if(this.clubService.requiresHistoryUpdate(process.getClubId())) {
                process.setState(ProcessState.UPDATE_HISTORY);
            } else {
                process.markDone();

            }
        } else {
            process.setState(ProcessState.CREATE_NEW_CLUB);
        }
    }
}
