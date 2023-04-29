package io.busata.fourleft.domain.aggregators;

import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Event;

import java.util.stream.Stream;

public interface EventSupplier {

    Stream<Event> getEvents(Club club);
}
