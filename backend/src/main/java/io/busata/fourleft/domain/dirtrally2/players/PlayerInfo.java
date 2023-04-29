package io.busata.fourleft.domain.dirtrally2.players;

import io.busata.fourleft.api.models.ControllerType;
import io.busata.fourleft.api.models.Platform;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class PlayerInfo {

    @Id
    @GeneratedValue
    UUID id;

    @Column(unique = true)
    String racenet;

    @Enumerated(EnumType.STRING)
            @Setter
    Platform platform;

    @Enumerated(EnumType.STRING)
    @Setter

    ControllerType controller;

    @Setter
    boolean syncedPlatform;

    @Setter
    boolean isOutdated;

    public PlayerInfo(String racenet) {
        this.racenet = racenet;
        this.platform = Platform.UNKNOWN;
        this.controller = ControllerType.UNKNOWN;
        this.syncedPlatform = false;
        this.isOutdated = false;
    }
}
