package io.busata.fourleft.api.models.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FixedPointsCalculatorTo extends PointsCalculatorTo {

    private int joinChampionshipsCount;
    UUID offsetChampionship;
    PointSystemTo pointSystemTo;
}
