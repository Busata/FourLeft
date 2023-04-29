package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Event;

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
