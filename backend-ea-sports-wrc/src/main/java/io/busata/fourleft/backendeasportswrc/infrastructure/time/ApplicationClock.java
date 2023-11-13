package io.busata.fourleft.backendeasportswrc.infrastructure.time;

import java.time.Clock;
import java.time.LocalDateTime;

public class ApplicationClock {
        public static final ThreadLocal<Clock> CLOCK =
                ThreadLocal.withInitial(Clock::systemUTC);

        public static LocalDateTime now() {
            return LocalDateTime.now(CLOCK.get());
        }
}
