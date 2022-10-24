package io.busata.fourleft.domain.configuration.points;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PointPair {

    Integer rank;
    Integer points;
}
