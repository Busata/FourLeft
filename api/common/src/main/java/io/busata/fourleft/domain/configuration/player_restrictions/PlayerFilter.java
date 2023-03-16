package io.busata.fourleft.domain.configuration.player_restrictions;

import lombok.Getter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
public class PlayerFilter {

    @Id
    @GeneratedValue
    UUID id;

    String name;

    @Enumerated(EnumType.STRING)
    PlayerFilterType filterType;


    @ElementCollection
    List<String> playerNames;
}
