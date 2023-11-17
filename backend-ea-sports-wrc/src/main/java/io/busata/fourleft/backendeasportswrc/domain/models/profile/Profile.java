package io.busata.fourleft.backendeasportswrc.domain.models.profile;

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

    public Profile(String id, String racenet, String discordId, Platform platform, boolean tracking) {
        this.id = id;
        this.displayName = racenet;
        this.discordId = discordId;
        this.platform = platform;
        this.controller = ControllerType.UNKNOWN;
        this.peripheral = PeripheralType.UNKNOWN;
        this.racenet = racenet;
        this.trackDiscord = tracking;
    }
}
