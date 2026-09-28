package io.busata.fourleft.backendeasportswrc.domain.models.tiers;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A named set of ordered tiers ("JRC 1", "JRC 2", ...) with players assigned to them. A player sits in at
 * most one tier of the set. Standalone for now — not yet attached to a channel or its clubs.
 */
@Entity
@Getter
@NoArgsConstructor
public class TierSet {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private Long guildId;

    private String createdBy;

    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tier_set_id", nullable = false)
    @OrderBy("position")
    private List<Tier> tiers = new ArrayList<>();

    public TierSet(String name, Long guildId, String createdBy) {
        this.name = name;
        this.guildId = guildId;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void rename(String name) {
        this.name = name;
    }

    public Tier addTier(String label) {
        Tier tier = new Tier(tiers.size(), label);
        tiers.add(tier);
        return tier;
    }

    public Optional<Tier> findTier(UUID tierId) {
        return tiers.stream().filter(tier -> Objects.equals(tier.getId(), tierId)).findFirst();
    }

    /** Removes the tier (its players become unassigned) and closes the gap in the positions. */
    public boolean removeTier(UUID tierId) {
        boolean removed = tiers.removeIf(tier -> Objects.equals(tier.getId(), tierId));
        renumber();
        return removed;
    }

    /**
     * Reorders the tiers to follow {@code tierIds}; ids not in the set are ignored and tiers missing from the
     * list keep their relative order after the listed ones.
     */
    public void reorder(List<UUID> tierIds) {
        tiers.sort(Comparator.comparingInt(tier -> {
            int index = tierIds.indexOf(tier.getId());
            return index < 0 ? Integer.MAX_VALUE : index;
        }));
        renumber();
    }

    /** Puts the player in the given tier, taking them out of any other tier of this set. */
    public boolean assignPlayer(UUID tierId, String playerId, String displayName) {
        Optional<Tier> target = findTier(tierId);
        if (target.isEmpty()) {
            return false;
        }
        unassignPlayer(playerId);
        target.get().addPlayer(new TierPlayer(playerId, displayName));
        return true;
    }

    public boolean unassignPlayer(String playerId) {
        boolean removed = false;
        for (Tier tier : tiers) {
            removed |= tier.removePlayer(playerId);
        }
        return removed;
    }

    public Optional<Tier> tierOf(String playerId) {
        return tiers.stream().filter(tier -> tier.hasPlayer(playerId)).findFirst();
    }

    private void renumber() {
        for (int i = 0; i < tiers.size(); i++) {
            tiers.get(i).setPosition(i);
        }
    }
}
