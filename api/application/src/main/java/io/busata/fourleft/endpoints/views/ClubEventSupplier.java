package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;

import java.util.Optional;

public interface ClubEventSupplier {

    Optional<Event> getEvent(Club club);
}
