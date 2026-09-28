package io.busata.fourleft.backendeasportswrc.domain.models.tiers;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** One tier of a {@link TierSet}; position 0 is the top tier. */
@Entity
@Getter
@NoArgsConstructor
public class Tier {

    @Id
    @GeneratedValue
    private UUID id;

    @Setter(AccessLevel.PACKAGE)
    private int position;

    @Setter
    private String label;

    @ElementCollection
    @CollectionTable(name = "tier_player", joinColumns = @JoinColumn(name = "tier_id"))
    private Set<TierPlayer> players = new HashSet<>();

    Tier(int position, String label) {
        this.position = position;
        this.label = label;
    }

    boolean hasPlayer(String playerId) {
        return players.stream().anyMatch(player -> Objects.equals(player.getPlayerId(), playerId));
    }

    void addPlayer(TierPlayer player) {
        players.add(player);
    }

    boolean removePlayer(String playerId) {
        return players.removeIf(player -> Objects.equals(player.getPlayerId(), playerId));
    }
}
