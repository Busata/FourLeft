package io.busata.fourleft.backendeasportswrc.domain.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One club tracked by a channel. The label names the class or tier ("Rally2", "JRC 1") and may be null. */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelClub {

    String clubId;

    String label;
}
