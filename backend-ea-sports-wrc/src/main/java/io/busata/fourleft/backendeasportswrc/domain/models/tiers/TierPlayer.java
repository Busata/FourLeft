package io.busata.fourleft.backendeasportswrc.domain.models.tiers;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A player in a tier: the racenet ssid plus the display name at the time of assignment. */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TierPlayer {

    private String playerId;

    private String displayName;
}
