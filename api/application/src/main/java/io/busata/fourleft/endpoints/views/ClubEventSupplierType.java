package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.domain.clubs.models.Club;

public enum ClubEventSupplierType {

    CURRENT {
        @Override
        public ClubEventSupplier getSupplier() {
            return Club::getCurrentEvent;
        }
    },
    PREVIOUS {
        @Override
        public ClubEventSupplier getSupplier() {
            return Club::getPreviousEvent;
        }
    };


    public abstract ClubEventSupplier getSupplier();
}
