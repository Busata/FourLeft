package io.busata.fourleft.domain.configuration.results_views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommunityChallengeView extends ResultsView {

    boolean postDailies;
    boolean postWeeklies;
    boolean postMonthlies;

    @Enumerated(EnumType.STRING)
    BadgeType badgeType;
}
