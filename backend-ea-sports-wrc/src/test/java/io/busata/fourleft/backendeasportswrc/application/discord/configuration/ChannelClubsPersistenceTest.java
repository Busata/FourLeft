package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.ChannelClub;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.common.ChannelClubMode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The channel -> clubs collection (V033) against real Postgres: ordering (index 0 = primary), the legacy
 * club_id mirror that keeps a rollback working, club-driven lookups reaching non-primary clubs, and the
 * FK cascade under the repository's bulk delete.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
class ChannelClubsPersistenceTest {

    @TestConfiguration
    static class RabbitStubConfig {
        @Bean
        ConnectionFactory connectionFactory() {
            return Mockito.mock(ConnectionFactory.class);
        }
    }

    @Autowired
    TestEntityManager em;

    @Autowired
    DiscordClubConfigurationRepository repository;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void singleClubConfigurationKeepsItsShape() {
        UUID id = (UUID) em.persistAndGetId(new DiscordClubConfiguration(1L, 100L, "146", true));
        em.flush();
        em.clear();

        DiscordClubConfiguration reloaded = em.find(DiscordClubConfiguration.class, id);

        assertThat(reloaded.getMode()).isEqualTo(ChannelClubMode.SINGLE);
        assertThat(reloaded.getClubIds()).containsExactly("146");
        assertThat(reloaded.getPrimaryClubId()).isEqualTo("146");
        assertThat(legacyClubId(id)).isEqualTo("146");
    }

    @Test
    void additionalClubsKeepOrderAndAreFoundByClub() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 101L, "jrc-1", true);
        config.setMode(ChannelClubMode.TIERED);
        config.addClub("jrc-2", "JRC 2");
        config.addClub("jrc-3", "JRC 3");
        UUID id = (UUID) em.persistAndGetId(config);
        em.flush();
        em.clear();

        DiscordClubConfiguration reloaded = em.find(DiscordClubConfiguration.class, id);
        assertThat(reloaded.getMode()).isEqualTo(ChannelClubMode.TIERED);
        assertThat(reloaded.getClubIds()).containsExactly("jrc-1", "jrc-2", "jrc-3");
        assertThat(reloaded.getClubs()).extracting(ChannelClub::getLabel).containsExactly(null, "JRC 2", "JRC 3");

        assertThat(repository.findByClubId("jrc-2")).extracting(DiscordClubConfiguration::getId).containsExactly(id);
        assertThat(repository.findByClubId("jrc-1")).extracting(DiscordClubConfiguration::getId).containsExactly(id);
        assertThat(repository.findByClubId("other")).isEmpty();
    }

    @Test
    void removingThePrimaryPromotesTheNextClub() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 102L, "a", true);
        config.addClub("b", null);
        config.addClub("c", null);
        UUID id = (UUID) em.persistAndGetId(config);
        em.flush();
        em.clear();

        DiscordClubConfiguration reloaded = em.find(DiscordClubConfiguration.class, id);
        reloaded.removeClub("a");
        em.flush();
        em.clear();

        assertThat(em.find(DiscordClubConfiguration.class, id).getClubIds()).containsExactly("b", "c");
        assertThat(legacyClubId(id)).isEqualTo("b");
    }

    @Test
    void addingATrackedClubRelabelsInsteadOfDuplicating() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 103L, "a", true);

        assertThat(config.addClub("a", "Rally2")).isFalse();
        assertThat(config.getClubs()).hasSize(1);
        assertThat(config.getClubs().get(0).getLabel()).isEqualTo("Rally2");
    }

    @Test
    void bulkDeleteByChannelCascadesToClubs() {
        DiscordClubConfiguration config = new DiscordClubConfiguration(1L, 104L, "a", true);
        config.addClub("b", null);
        UUID id = (UUID) em.persistAndGetId(config);
        em.flush();
        em.clear();

        repository.removeByChannelId(104L);
        em.flush();

        Integer remaining = jdbc.queryForObject(
                "select count(*) from discord_club_configuration_club where configuration_id = ?", Integer.class, id);
        assertThat(remaining).isZero();
    }

    private String legacyClubId(UUID id) {
        return jdbc.queryForObject("select club_id from discord_club_configuration where id = ?", String.class, id);
    }
}
