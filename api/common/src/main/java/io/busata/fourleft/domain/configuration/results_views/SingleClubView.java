package io.busata.fourleft.domain.configuration.results_views;

import io.busata.fourleft.domain.configuration.player_restrictions.PlayerFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SingleClubView extends ResultsView  {

    long clubId;

    String name;

    @ElementCollection
    List<Integer> powerStageIndices;

    @ManyToOne(cascade = CascadeType.ALL)
    PlayerFilter playerFilter;

    @Override
    public Set<Long> getAssociatedClubs() {
        return Set.of(clubId);
    }

    public boolean hasPowerStage() {
        return !this.powerStageIndices.isEmpty();
    }
}