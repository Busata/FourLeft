package io.busata.fourleft.domain.configuration.player_restrictions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerFilter {

    @Id
    @GeneratedValue
    UUID id;

    @Enumerated(EnumType.STRING)
    PlayerFilterType filterType;

    @ElementCollection
    List<String> racenetNames;
}
