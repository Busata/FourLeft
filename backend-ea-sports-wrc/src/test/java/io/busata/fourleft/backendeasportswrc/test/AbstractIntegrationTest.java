package io.busata.fourleft.backendeasportswrc.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.spring.api.DBRider;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static com.github.database.rider.core.api.configuration.Orthography.LOWERCASE;

@SpringBootTest
@ActiveProfiles("test")
@DBRider
@DBUnit(schema = "public", caseInsensitiveStrategy = LOWERCASE, cacheConnection = false)
public abstract class AbstractIntegrationTest {


    @Autowired
    protected ObjectMapper mapper;

    public void setClock(LocalDateTime localDateTime) {
        ApplicationClock.CLOCK.set(Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneId.systemDefault()));
    }


    @BeforeEach()
    public void resetClock() {
        ApplicationClock.CLOCK.set(Clock.systemDefaultZone());
    }

    @AfterEach
    public void removeClock() {
        ApplicationClock.CLOCK.remove();
    }

}
