package io.busata.fourleft.domain.views.configuration.points;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PointsCalculator {
    @Id
    @GeneratedValue
    UUID id;
}
