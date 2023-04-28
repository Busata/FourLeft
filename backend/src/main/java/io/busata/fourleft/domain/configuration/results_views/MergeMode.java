package io.busata.fourleft.domain.configuration.results_views;

import java.time.Duration;

public enum MergeMode {
    ADD_TIMES {
        @Override
        public Duration merge(Duration a, Duration b) {
            return a.plus(b);
        }
    },
    TAKE_BEST {
        @Override
        public Duration merge(Duration a, Duration b) {
            return (a.compareTo(b) <= 0) ? a : b;
        }
    };


    public abstract Duration merge(Duration a, Duration b);
}
