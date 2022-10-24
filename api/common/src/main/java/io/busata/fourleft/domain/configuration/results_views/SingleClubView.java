package io.busata.fourleft.domain.configuration.results_views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SingleClubView extends ResultsView implements ResultProperties {

    long clubId;
    boolean usePowerStage;
    int defaultPowerstageIndex; // -1 for last.

    @Enumerated(EnumType.STRING)
    BadgeType badgeType;

    @Enumerated(EnumType.STRING)
    PlayerRestrictions playerRestriction;

    @ElementCollection
    List<String> players;

    @Override
    public boolean isPowerStage() {
        return this.usePowerStage;
    }

    @Override
    public int getPowerStageIndex() {
        return defaultPowerstageIndex;
    }

    @Override
    public List<String> getPlayerNames() {
        return this.players;
    }

    @Override
    public PlayerRestrictions getPlayerRestrictions() {
        return this.playerRestriction;
    }
}
