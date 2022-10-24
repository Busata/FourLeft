package io.busata.fourleft.domain.configuration.points;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class FixedPointsCalculator extends PointsCalculator {

    int joinChampionshipsCount;
    UUID offsetChampionship;

    @OneToOne
    PointSystem pointSystem;


}
