package io.busata.fourleft.domain.configuration.results_views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartitionElement {

    @Id
    @GeneratedValue
    UUID id;

    String name;

    int customOrder;

    @ElementCollection
    List<String> racenetNames;

}
