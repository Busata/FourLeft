package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchorEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
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

import java.math.BigDecimal;
import java.util.List;
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
    void pointAnchorConfigRoundTripsThroughJsonb() {
        // Also proves V028's scoring_anchors column + JSON seed are valid: Flyway must apply V028 before
        // this context starts, so a green run means the migration parsed and executed against real Postgres.
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 44L, "777", true);
        config.setCustomScoringEnabled(true);
        config.setScoringStrategy(ScoringStrategy.POINT_ANCHOR);
        config.setScoringAnchors(new ScoringAnchors(1, List.of(
                new ScoringAnchorEntry(1, 2500, null),
                new ScoringAnchorEntry(204, null, new BigDecimal("1.83")),
                new ScoringAnchorEntry(788, 424, null))));

        UUID id = (UUID) em.persistAndGetId(config);
        em.flush();
        em.clear();

        DiscordClubConfiguration reloaded = em.find(DiscordClubConfiguration.class, id);

        assertThat(reloaded.getScoringStrategy()).isEqualTo(ScoringStrategy.POINT_ANCHOR);
        ScoringAnchors anchors = reloaded.getScoringAnchors();
        assertThat(anchors).isNotNull();
        assertThat(anchors.floor()).isEqualTo(1);
        assertThat(anchors.entries()).hasSize(3);
        assertThat(anchors.entries().get(0).points()).isEqualTo(2500);
        assertThat(anchors.entries().get(0).isAnchor()).isTrue();
        assertThat(anchors.entries().get(1).decrease()).isEqualByComparingTo("1.83");
        assertThat(anchors.entries().get(1).isAnchor()).isFalse();
        assertThat(anchors.entries().get(2).points()).isEqualTo(424);
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
