package io.busata.fourleft.backendeasportswrc.domain.models.profile;

import io.busata.fourleft.api.easportswrc.models.ProfileClaimState;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.PeripheralType;
import io.busata.fourleft.common.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Profile {

    @Id
    String id;

    @Column(unique = true)
    @Setter
    String displayName;

    String discordId;

    @Enumerated(EnumType.STRING)
    @Setter
    Platform platform;

    @Enumerated(EnumType.STRING)
    @Setter
    ControllerType controller;

    @Enumerated(EnumType.STRING)
    @Setter
    PeripheralType peripheral;

    String racenet;

    @Setter
    boolean trackDiscord;

    @Enumerated(EnumType.STRING)
    ProfileClaimState claimState;

    String disputedByDiscordId;

    LocalDateTime disputedAt;

    public Profile(String id, String racenet, String discordId, Platform platform, boolean tracking) {
        this.id = id;
        this.displayName = racenet;
        this.discordId = discordId;
        this.platform = platform;
        this.controller = ControllerType.UNKNOWN;
        this.peripheral = PeripheralType.UNKNOWN;
        this.racenet = racenet;
        this.trackDiscord = tracking;
        this.claimState = ProfileClaimState.CLAIMED;
    }

    public boolean isHeldBy(String discordId) {
        return this.discordId != null && this.discordId.equals(discordId);
    }

    /** Hands the profile to a (new) discord user, as a fresh unchallenged claim. */
    public void claimBy(String discordId) {
        this.discordId = discordId;
        this.claimState = ProfileClaimState.CLAIMED;
        this.disputedByDiscordId = null;
        this.disputedAt = null;
    }

    public void disputeBy(String discordId) {
        this.claimState = ProfileClaimState.DISPUTED;
        this.disputedByDiscordId = discordId;
        this.disputedAt = ApplicationClock.now();
    }

    /** The dispute ends in the holder's favour: withdrawn by the disputer, or dismissed by an operator. */
    public void clearDispute() {
        claimBy(this.discordId);
    }

    /** An operator sided with the disputer: they take over the profile. */
    public void transferToDisputer() {
        claimBy(this.disputedByDiscordId);
    }

    /** The holder claimed another racenet account; this one is up for grabs again. */
    public void release() {
        claimBy(null);
    }
}
