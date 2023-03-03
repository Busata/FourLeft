package io.busata.fourleft.endpoints.views.points.factory.tiers.helpers;

import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FixedPointChampionshipFetcher {
    public List<Championship> filterChampionships(List<Championship> championships, FixedPointsCalculator calc) {
        championships = new ArrayList<>(championships);

        if(calc.getJoinChampionshipsCount() == 1) {

            championships = championships.stream().sorted(Comparator.comparing(Championship::getOrder)).collect(Collectors.toList());
            Collections.reverse(championships);

            return championships.stream().limit(1).collect(Collectors.toList());
        }




        championships = championships.stream().filter(Championship::isInActive).sorted(Comparator.comparing(Championship::getOrder))
                .collect(Collectors.toList());

        if (calc.getOffsetChampionship() != null) {
            championships = championships.stream()
                    .dropWhile(championship -> !championship.getReferenceId().equals(calc.getOffsetChampionship()))
                    .collect(Collectors.toList());
        }

        var requiredChampionships = calculateRequiredChampionships(championships, calc);

        Collections.reverse(championships);

        return championships.stream().limit(requiredChampionships).collect(Collectors.toList());
    }

    private int calculateRequiredChampionships(List<Championship> championships, FixedPointsCalculator calc) {
        var totalSize = championships.size();
        int moduloChampionships = totalSize % calc.getJoinChampionshipsCount();

        if(moduloChampionships == 0) {
            return calc.getJoinChampionshipsCount();
        } else {
            return moduloChampionships;
        }
    }
}
