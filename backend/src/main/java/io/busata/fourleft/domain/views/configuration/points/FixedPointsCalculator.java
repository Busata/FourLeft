package io.busata.fourleft.domain.views.configuration.points;


import io.busata.fourleft.domain.dirtrally2.clubs.models.Championship;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class FixedPointsCalculator extends PointsCalculator {

    int joinChampionshipsCount;
    String offsetChampionship;

    @OneToOne(cascade = CascadeType.ALL)
    PointSystem pointSystem;


    public boolean isNotOffsetChampionship(Championship championship) {
        if (this.offsetChampionship == null) {
            return false;
        }

        return !championship.getReferenceId().equals(offsetChampionship);
    }

}
