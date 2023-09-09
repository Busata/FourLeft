package io.busata.fourleft.domain.dirtrally2.players;

import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.Platform;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor()
public class PlayerInfo {

    @Id
    @GeneratedValue
    UUID id;

    @Column(unique = true)
    @Setter
    String displayName;

    String discordId;

    @Column
    @ElementCollection
    Set<String> racenets = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Setter
    Platform platform;

    @Enumerated(EnumType.STRING)
    @Setter
    ControllerType controller;

    @Setter
    boolean syncedPlatform;

    public PlayerInfo(String racenet) {
        this.displayName = racenet;
        this.racenets.add(racenet);
        this.platform = Platform.UNKNOWN;
        this.controller = ControllerType.UNKNOWN;
        this.syncedPlatform = false;
    }
}
