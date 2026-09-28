package io.busata.fourleft.backendeasportswrc.domain.models.tiers;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** A private edit link for a tier set; the UUID is the credential, like a channel configuration request. */
@Entity
@Getter
@NoArgsConstructor
public class TierSetLink {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID tierSetId;

    private String discordId;

    private LocalDateTime createdAt;

    public TierSetLink(UUID tierSetId, String discordId) {
        this.tierSetId = tierSetId;
        this.discordId = discordId;
        this.createdAt = LocalDateTime.now();
    }
}
