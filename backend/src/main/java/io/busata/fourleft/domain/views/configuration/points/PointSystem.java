package io.busata.fourleft.domain.views.configuration.points;

import io.busata.fourleft.api.models.PointPair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PointSystem {
    @Id
    @GeneratedValue
    UUID id;

    String description;

    //Points given if the player did not dnf but falls outside the points specified below
    int defaultRankingPoint = 1;
    int defaultPowerstagePoint = 0;

    @ElementCollection
    private List<PointPair> rankingPoints;

    @ElementCollection
    private List<PointPair> powerStagePoints;

    public Integer getPoints(int rank) {
        if(rank > rankingPoints.size()) {
            return defaultRankingPoint;
        } else {
            return rankingPoints.get(rank - 1).getPoint();
        }
    }

    public Integer getPowerStagePoints(int rank) {
        if(rank > powerStagePoints.size()) {
            return defaultPowerstagePoint;
        } else {
            return powerStagePoints.get(rank - 1).getPoint();
        }
    }
}
