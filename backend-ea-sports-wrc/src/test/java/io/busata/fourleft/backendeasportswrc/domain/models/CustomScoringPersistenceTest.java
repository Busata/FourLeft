package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.common.ScoringStrategy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the one thing with no compile-time guarantee: the {@code Map<String,Integer>} scoring table
 * round-trips through the jsonb column. Runs against a real Postgres (Testcontainers via the "test"
 * profile) with all Flyway migrations applied — so it also proves V027's ~11 KB jsonb seed literal is
 * valid, since Flyway must apply it before the context starts. Uses the JPA slice only (no RabbitMQ).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
class CustomScoringPersistenceTest {

    // The app's RabbitMQConfiguration is picked up even in a JPA slice and needs a ConnectionFactory.
    // No @RabbitListener beans load here, so a mock is enough to satisfy the wiring without a broker.
    @TestConfiguration
    static class RabbitStubConfig {
        @Bean
        ConnectionFactory connectionFactory() {
            return Mockito.mock(ConnectionFactory.class);
        }
    }

    @Autowired
    TestEntityManager em;

    @Test
    void scoringConfigRoundTripsThroughJsonb() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 42L, "999", true);
        config.setCustomScoringEnabled(true);
        config.setScoringStrategy(ScoringStrategy.LOOKUP_TABLE);
        config.setScoringTable(Map.of("1", 2500, "2", 2200, "10", 1925));

        UUID id = (UUID) em.persistAndGetId(config);
        em.flush();
        em.clear();

        DiscordClubConfiguration reloaded = em.find(DiscordClubConfiguration.class, id);

        assertThat(reloaded.isCustomScoringEnabled()).isTrue();
        assertThat(reloaded.getScoringStrategy()).isEqualTo(ScoringStrategy.LOOKUP_TABLE);
        assertThat(reloaded.getScoringTable())
                .hasSize(3)
                .containsEntry("1", 2500)
                .containsEntry("2", 2200)
                .containsEntry("10", 1925);
    }

    @Test
    void defaultsAreSaneForANewConfig() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 43L, "888", true);

        UUID id = (UUID) em.persistAndGetId(config);
        em.flush();
        em.clear();

        DiscordClubConfiguration reloaded = em.find(DiscordClubConfiguration.class, id);

        assertThat(reloaded.isCustomScoringEnabled()).isFalse();
        assertThat(reloaded.getScoringStrategy()).isEqualTo(ScoringStrategy.LOOKUP_TABLE);
        assertThat(reloaded.getScoringTable()).isEmpty();
    }
}
