package io.busata.fourleft.domain.clubs.services;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.List.of;

@Service
@RequiredArgsConstructor
public class BoardEntryFetcher {
    private final LeaderboardRepository leaderboardRepository;

    public List<BoardEntry> create(SingleClubView view, Event event) {
        var lastStageEntries = getEntries(event, event.getLastStage());
        if (view.isPowerStage() && view.getPowerStageIndex() != -1) {
            var powerStageEntries = getPowerStageEntries(view, event);
            return mergeEntries(lastStageEntries, powerStageEntries);
        }
        return lastStageEntries;
    }

    public List<BoardEntry> create(TieredView view, Event event) {
        var lastStageEntries = getEntries(event, event.getLastStage());
        if (view.isUsePowerStage() && view.getDefaultPowerstageIndex() != -1) {
            var powerStageEntries = getPowerStageEntries(view, event);
            return mergeEntries(lastStageEntries, powerStageEntries);
        }
        return lastStageEntries;
    }

    private List<BoardEntry> mergeEntries(List<BoardEntry> entries, List<BoardEntry> powerStageEntries) {
        if (powerStageEntries.size() == 0) {
            return entries;
        }

        return entries.stream().map(entry -> {
            return powerStageEntries.stream().filter(powerStageEntry -> powerStageEntry.getName().equalsIgnoreCase(entry.getName())).findFirst()
                    .map(powerStageEntry -> {
                        entry.setStageDiff(powerStageEntry.getStageDiff());
                        entry.setStageTime(powerStageEntry.getStageTime());
                        return entry;
                    }).orElse(entry);

        }).collect(Collectors.toList());
    }

    private List<BoardEntry> getEntries(Event event, Stage stage) {
        return leaderboardRepository.findLeaderboardByChallengeIdAndEventIdAndStageId(event.getChallengeId(), event.getReferenceId(), String.valueOf(stage.getReferenceId()))
                .stream().
                flatMap(board -> board.getEntries().stream())
                .toList();
    }

    private List<BoardEntry> getPowerStageEntries(SingleClubView view, Event event) {
        if (view.isPowerStage()) {
            var stages = event.getStages();
            var powerStage = view.getPowerStageIndex() < 0 ? stages.get(stages.size() + view.getPowerStageIndex()) : stages.get(view.getPowerStageIndex());
            return getEntries(event, powerStage);
        } else {
            return of();
        }
    }

    private List<BoardEntry> getPowerStageEntries(TieredView view, Event event) {
        if (view.isUsePowerStage()) {
            var stages = event.getStages();
            var powerStage = view.getDefaultPowerstageIndex() < 0 ? stages.get(stages.size() + view.getDefaultPowerstageIndex()) : stages.get(view.getDefaultPowerstageIndex());
            return getEntries(event, powerStage);
        } else {
            return of();
        }
    }
}
