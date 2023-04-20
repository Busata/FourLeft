package io.busata.fourleft.api.models.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FixedPointsCalculatorTo extends PointsCalculatorTo {

    private int joinChampionshipsCount;
    String offsetChampionship;
    PointSystemTo pointSystem;
}
