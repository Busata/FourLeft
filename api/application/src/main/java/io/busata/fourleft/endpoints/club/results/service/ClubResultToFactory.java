package io.busata.fourleft.endpoints.club.results.service;

import io.busata.fourleft.api.models.ChampionshipEventEntryTo;
import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import io.busata.fourleft.api.models.ChampionshipStageEntryTo;
import io.busata.fourleft.api.models.ChampionshipStageSummaryTo;
import io.busata.fourleft.api.models.ChampionshipStandingEntryTo;
import io.busata.fourleft.api.models.ClubResultTo;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.domain.clubs.models.StandingEntry;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.List.of;

@Component
@RequiredArgsConstructor
public class ClubResultToFactory {
    private final LeaderboardRepository leaderboardRepository;
    private final ResultEntryToFactory resultEntryToFactory;

    public ClubResultTo create(Event event) {
        final var lastStage = event.getLastStage();

        final List<BoardEntry> entries = getEntries(event);

        return new ClubResultTo(
                event.getReferenceId(),
                event.getChallengeId(),
                event.getName(),
                lastStage.getName(),
                event.getStages().stream().map(Stage::getName).collect(Collectors.toList()),
                event.getVehicleClass(),
                event.getCountry(),
                event.getLastResultCheckedTime(),
                event.getEndTime(),
                resultEntryToFactory.create(entries)
        );
    }

    private List<BoardEntry> getEntries(Event event) {
        return leaderboardRepository.findLeaderboardByChallengeIdAndEventIdAndStageId(event.getChallengeId(), event.getReferenceId(), String.valueOf(event.getLastStage().getReferenceId()))
                .stream().
                flatMap(board -> board.getEntries().stream())
                .toList();
    }

    public List<ChampionshipStandingEntryTo> createStandingsSummary(List<StandingEntry> entries) {
        return entries.stream().map(entry -> {
            return new ChampionshipStandingEntryTo(entry.getRank(), entry.getNationality(), entry.getDisplayName(), entry.getTotalPoints());
        }).collect(Collectors.toList());
    }

    public ChampionshipEventSummaryTo createEventSummary(Championship championship) {
        return new ChampionshipEventSummaryTo(
                championship.getName(),
                championship.isHardcoreDamage(),
                championship.isAllowAssists(),
                championship.isUnexpectedMoments(),
                championship.isForceCockpitCamera(),
                championship.getEvents().stream().sorted(Comparator.comparing(Event::getReferenceId)).map(event -> {
                    var country = event.getCountry();
                    var stage = event.getStages().get(event.getStages().size() - 1);
                    var firstStageCondition = event.getFirstStageCondition();
                    var vehicleClass = event.getVehicleClass();
                    var isCurrent = event.isCurrent();
                    var isFinished = event.isPrevious();

                    return new ChampionshipEventEntryTo(country,
                            event.getName(),
                            stage.getName(),
                            event.getChallengeId(),
                            event.getReferenceId(),
                            stage.getReferenceId(),
                            firstStageCondition,
                            vehicleClass,
                            isCurrent,
                            isFinished);
                }).collect(Collectors.toList())
        );
    }

    public ChampionshipStageSummaryTo createStageSummary(Championship championship) {
        return new ChampionshipStageSummaryTo(
                championship.getName(),
                championship.isHardcoreDamage(),
                championship.isAllowAssists(),
                championship.isUnexpectedMoments(),
                championship.isForceCockpitCamera(),
                championship.getEvents().stream().findFirst().map(event -> {
                    var country = event.getCountry();
                    var vehicleClass = event.getVehicleClass();
                    var stages = event.getStages().stream().map(Stage::getName).collect(Collectors.toList());

                    return new ChampionshipStageEntryTo(country, vehicleClass, stages);
                }).orElseThrow()
        );
    }
}
