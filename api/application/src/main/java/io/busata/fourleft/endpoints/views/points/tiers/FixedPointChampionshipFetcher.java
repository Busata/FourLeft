package io.busata.fourleft.endpoints.views.points.tiers;

import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FixedPointChampionshipFetcher {
    public List<Championship> filterChampionships(List<Championship> championships, FixedPointsCalculator calc, PointsPeriod period) {

        if (calc.getOffsetChampionship() != null) {

            championships = championships.stream()
                    .sorted(Comparator.comparing(Championship::getOrder))
                    .dropWhile(championship -> championship.getId() != calc.getOffsetChampionship())
                    .collect(Collectors.toList());
        }

        championships = new ArrayList<>(championships);

        Collections.reverse(championships);

        var skipChampionships = 0;
        if (period == PointsPeriod.PREVIOUS) {
            skipChampionships = calc.getJoinChampionshipsCount();
        }

        return championships.stream().skip(skipChampionships).limit(calc.getJoinChampionshipsCount()).collect(Collectors.toList());
    }
}
