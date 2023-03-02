package io.busata.fourleft.domain.configuration.event_restrictions.models;

import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToMany
    @JoinTable(name = "player_tiers",
            joinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "single_club_view", referencedColumnName = "id")
    )
    List<SingleClubView> tiers = new ArrayList<>();

    @Getter
    String racenet;

    public Player(String racenet) {
        this.racenet = racenet;
    }

    public void setTier(SingleClubView tier) {
        this.clearTiers();
        this.tiers.add(tier);
    }

    public void clearTiers() {
        this.tiers.clear();
    }
}
