package io.busata.fourleft.backendeasportswrc.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class ClubConfiguration {

    @GeneratedValue
    @Id
    Long id;

    String clubId;

    boolean keepSynced;

    //Automate stuff
}
