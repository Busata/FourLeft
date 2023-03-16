package io.busata.fourleft.domain.configuration.results_views;

import io.busata.fourleft.domain.configuration.player_restrictions.PlayerFilter;
import lombok.Getter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
public class PartitionElement {

    @Id
    @GeneratedValue
    UUID id;

    String name;

    @ManyToOne
    @JoinColumn(name="partition_view_id")
    PartitionView partitionView;

    int order;

    @ManyToOne
    PlayerFilter filter;

}
