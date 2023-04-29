package io.busata.fourleft.domain.dirtrally2.clubs.models;


import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Table(name = "unique_players")
public class UniquePlayersView {

    @Id
    String name;

    Long occurrence;
}
