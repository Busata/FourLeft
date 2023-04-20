package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;

import java.util.stream.Stream;

public interface EventSupplier {

    Stream<Event> getEvents(Club club);
}
