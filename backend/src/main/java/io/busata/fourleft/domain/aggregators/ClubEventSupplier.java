package io.busata.fourleft.domain.aggregators;

import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;

import java.util.stream.Stream;

public enum ClubEventSupplier implements EventSupplier {

    CURRENT {
        @Override
        public Stream<Event> getEvents(Club club) {
            return club.getCurrentEvent().stream();
        }
    },
    PREVIOUS {
        @Override
        public Stream<Event> getEvents(Club club) {
            return club.getPreviousEvent().stream();
        }
    };
}
