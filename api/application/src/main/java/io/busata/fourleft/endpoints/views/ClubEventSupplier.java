package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;

import java.util.Optional;

public enum ClubEventSupplier {

    CURRENT {
        @Override
        public Optional<Event> getEvent(Club club) {
            return club.getCurrentEvent();
        }
    },
    PREVIOUS {
        @Override
        public Optional<Event> getEvent(Club club) {
            return club.getPreviousEvent();
        }
    };




    public abstract Optional<Event> getEvent(Club club);
}
