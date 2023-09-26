package io.busata.fourleft.domain.aggregators.points;

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
    Integer point;
}
