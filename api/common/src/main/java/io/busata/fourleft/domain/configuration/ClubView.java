package io.busata.fourleft.domain.configuration;

import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ClubView {
    @Id
    @GeneratedValue
    UUID id;

    BadgeType badgeType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "results_view_id")
    ResultsView resultsView;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "points_calculator_id")
    PointsCalculator pointsCalculator;
}
