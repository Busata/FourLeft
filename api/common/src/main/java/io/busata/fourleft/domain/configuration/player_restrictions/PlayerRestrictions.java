package io.busata.fourleft.domain.configuration.player_restrictions;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import lombok.Getter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
public class PlayerRestrictions {

    @Id
    @GeneratedValue
    UUID id;

    @Enumerated(EnumType.STRING)
    PlayerRestrictionFilterType filterType;


    @ElementCollection
    List<String> playerNames;
}
