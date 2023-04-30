package io.busata.fourleft.application.aggregators.helpers;

import io.busata.fourleft.domain.dirtrally2.clubs.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.aggregators.points.FixedPointsCalculator;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FixedPointChampionshipFetcher {
    public List<Championship> filterChampionships(List<Championship> championships, FixedPointsCalculator calc) {
        if(calc.getJoinChampionshipsCount() == 1) {
            return takeLast(championships);
        }

        var inactiveOffsetChampionships = championships.stream()
                .filter(Championship::isInActive)
                .sorted(Comparator.comparing(Championship::getOrder))
                .dropWhile(calc::isNotOffsetChampionship)
                .sorted(Comparator.comparing(Championship::getOrder).reversed())
                .toList();

        var requiredChampionships = calculateRequiredChampionships(inactiveOffsetChampionships, calc.getJoinChampionshipsCount());

        return inactiveOffsetChampionships.stream().limit(requiredChampionships).collect(Collectors.toList());
    }

    private List<Championship> takeLast(List<Championship> championships) {
        return championships.stream()
                .sorted(Comparator.comparing(Championship::getOrder).reversed())
                .filter(championship -> championship.getEvents().stream().anyMatch(Event::isPrevious))
                .limit(1)
                .collect(Collectors.toList());
    }

    private int calculateRequiredChampionships(List<Championship> championships, int joinChampionshipCount) {
        var totalSize = championships.size();
        int moduloChampionships = totalSize % joinChampionshipCount;

        if(moduloChampionships == 0) {
            return joinChampionshipCount;
        } else {
            return moduloChampionships;
        }
    }
}
