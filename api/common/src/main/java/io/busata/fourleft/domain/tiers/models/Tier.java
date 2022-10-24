package io.busata.fourleft.domain.tiers.models;


import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import io.busata.fourleft.domain.configuration.results_views.ResultProperties;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.configuration.results_views.TiersView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter
public class Tier implements ResultProperties {

    @Id
    @Getter
    @GeneratedValue
    UUID id;

    @Getter
    Long clubId;

    @Getter
    String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tier_group_id")
    @Setter
    TiersView tiersView;


    @ManyToMany
    @JoinTable(name = "player_tiers",
            joinColumns = @JoinColumn(name = "tier_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id")
    )
    List<Player> players = new ArrayList<>();


    public Tier(Long clubId, String name) {
        this.clubId = clubId;
        this.name = name;
    }

    @Override
    public boolean isPowerStage() {
        return this.getTiersView().isUsePowerStage();
    }

    @Override
    public int getPowerStageIndex() {
        return this.getTiersView().getDefaultPowerstageIndex();
    }

    @Override
    public List<String> getPlayerNames() {
        return getPlayers().stream().map(Player::getRacenet).collect(Collectors.toList());
    }

    @Override
    public PlayerRestrictions getPlayerRestrictions() {
        return PlayerRestrictions.EXCLUDE;
    }

    @Override
    public BadgeType getBadgeType() {
        return this.getTiersView().getBadgeType();
    }
}
