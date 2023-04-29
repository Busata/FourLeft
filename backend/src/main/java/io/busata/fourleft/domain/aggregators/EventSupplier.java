package io.busata.fourleft.domain.aggregators;

import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;

import java.util.stream.Stream;

public interface EventSupplier {

    Stream<Event> getEvents(Club club);
}
