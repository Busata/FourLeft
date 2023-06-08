package io.busata.fourleft.domain.aggregators.points;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PointSystem {
    @Id
    @GeneratedValue
    UUID id;

    String description;

    //Points given if the player did not dnf but falls outside the point specified below
    int defaultRankingPoint = 1;
    int defaultPowerstagePoint = 0;

    @ElementCollection
    private List<PointPair> rankingPoints;

    @ElementCollection
    private List<PointPair> powerStagePoints;

    public Integer getPoints(int rank) {

        final var sortedPoints = rankingPoints.stream().sorted(Comparator.comparing(PointPair::getRank)).toList();

        if(rank > sortedPoints.size()) {
            return defaultRankingPoint;
        } else {
            return sortedPoints.get(rank - 1).getPoint();
        }
    }

    public Integer getPowerStagePoints(int rank) {
        final var sortedPoints = powerStagePoints.stream().sorted(Comparator.comparing(PointPair::getRank)).toList();

        if(rank > sortedPoints.size()) {
            return defaultPowerstagePoint;
        } else {
            return sortedPoints.get(rank - 1).getPoint();
        }
    }
}
