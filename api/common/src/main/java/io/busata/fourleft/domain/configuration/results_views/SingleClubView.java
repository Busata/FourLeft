package io.busata.fourleft.domain.configuration.results_views;

import io.busata.fourleft.domain.configuration.player_restrictions.PlayerRestrictionFilterType;
import io.busata.fourleft.domain.configuration.player_restrictions.PlayerRestrictions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SingleClubView extends ResultsView  {

    long clubId;
    boolean powerStage;
    String name;

    int powerStageIndex; // -1 for last.

    @Enumerated(EnumType.STRING)
    BadgeType badgeType;

    @ManyToOne
    @JoinColumn(name="player_restrictions_id")
    PlayerRestrictions playerRestrictions;

    @Override
    public List<Long> getAssociatedClubs() {
        return List.of(clubId);
    }
}
