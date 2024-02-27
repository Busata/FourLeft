package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Club;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;

public interface ResultBuilder<T> {

     T accept(Club club, Championship championship, Event event);
}
