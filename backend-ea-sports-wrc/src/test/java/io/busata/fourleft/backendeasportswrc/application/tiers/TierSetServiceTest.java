package io.busata.fourleft.backendeasportswrc.application.tiers;

import io.busata.fourleft.api.easportswrc.models.TierPlayerTo;
import io.busata.fourleft.api.easportswrc.models.TierSetLinkTo;
import io.busata.fourleft.api.easportswrc.models.TierSetTo;
import io.busata.fourleft.api.easportswrc.models.TierTo;
import io.busata.fourleft.backendeasportswrc.application.players.PlayerSuggestions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tier sets (V037) against real Postgres, driven through the service the endpoint uses. */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
@Import({TierSetService.class, PlayerSuggestions.class})
class TierSetServiceTest {

    @TestConfiguration
    static class RabbitStubConfig {
        @Bean
        ConnectionFactory connectionFactory() {
            return Mockito.mock(ConnectionFactory.class);
        }
    }

    @Autowired
    TierSetService service;

    @Autowired
    TestEntityManager em;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void createsASetWithAnEditLink() {
        TierSetLinkTo link = service.create("  JRC  ", 1L, "123");

        assertThat(link.name()).isEqualTo("JRC");
        assertThat(service.get(link.linkId())).get().extracting(TierSetTo::name).isEqualTo("JRC");
        assertThat(service.namesForGuild(1L)).containsExactly("JRC");
        assertThat(service.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void namesAreUniquePerGuildOnly() {
        service.create("JRC", 1L, "123");

        assertThatThrownBy(() -> service.create("jrc", 1L, "123")).isInstanceOf(ResponseStatusException.class);
        service.create("JRC", 2L, "123");
        // CLI-created sets have no guild and aren't checked.
        service.create("JRC", null, "cli");
        service.create("JRC", null, "cli");
    }

    @Test
    void requestLinkFindsTheGuildsSetByName() {
        TierSetLinkTo created = service.create("JRC", 1L, "123");

        TierSetLinkTo second = service.requestLink(1L, "jrc", "456").orElseThrow();

        assertThat(second.linkId()).isNotEqualTo(created.linkId());
        assertThat(service.get(second.linkId())).isEqualTo(service.get(created.linkId()));
        assertThat(service.requestLink(2L, "JRC", "456")).isEmpty();
    }

    @Test
    void tiersKeepTheirOrderAcrossAddReorderAndRemove() {
        UUID link = service.create("JRC", 1L, "123").linkId();
        service.addTier(link, "JRC 1");
        service.addTier(link, "JRC 2");
        TierSetTo three = service.addTier(link, "JRC 3").orElseThrow();
        List<UUID> ids = three.tiers().stream().map(TierTo::id).toList();
        assertThat(ids).doesNotContainNull();

        service.reorderTiers(link, List.of(ids.get(2), ids.get(0), ids.get(1)));
        service.relabelTier(link, ids.get(2), "Top");
        clear();
        assertThat(labels(link)).containsExactly("Top", "JRC 1", "JRC 2");

        service.removeTier(link, ids.get(0));
        clear();
        assertThat(labels(link)).containsExactly("Top", "JRC 2");
        assertThat(jdbc.queryForList("SELECT position FROM tier ORDER BY position", Integer.class)).containsExactly(0, 1);
    }

    @Test
    void aPlayerSitsInOneTierAtATime() {
        UUID link = service.create("JRC", 1L, "123").linkId();
        service.addTier(link, "JRC 1");
        List<UUID> ids = service.addTier(link, "JRC 2").orElseThrow().tiers().stream().map(TierTo::id).toList();

        service.assignPlayer(link, ids.get(0), "111", "Busata");
        service.assignPlayer(link, ids.get(0), "222", "alice");
        service.assignPlayer(link, ids.get(1), "111", "Busata");
        clear();

        TierSetTo set = service.get(link).orElseThrow();
        assertThat(set.tiers().get(0).players()).containsExactly(new TierPlayerTo("222", "alice"));
        assertThat(set.tiers().get(1).players()).containsExactly(new TierPlayerTo("111", "Busata"));

        service.unassignPlayer(link, "222");
        service.removeTier(link, ids.get(1));
        clear();
        assertThat(service.get(link).orElseThrow().tiers().get(0).players()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tier_player", Integer.class)).isZero();
    }

    @Test
    void assigningToAnUnknownTierFails() {
        UUID link = service.create("JRC", 1L, "123").linkId();

        assertThatThrownBy(() -> service.assignPlayer(link, UUID.randomUUID(), "111", "Busata"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void suggestsPlayersFromClubLeaderboards() {
        jdbc.update("INSERT INTO club_leaderboard_entry (id, ssid, display_name) VALUES (gen_random_uuid(), '1', 'Busata'), (gen_random_uuid(), '1', 'Busata'), (gen_random_uuid(), '2', 'Hayabusa'), (gen_random_uuid(), NULL, 'Busy')");
        UUID link = service.create("JRC", 1L, "123").linkId();

        assertThat(service.suggestPlayers(link, "BUS").orElseThrow())
                .containsExactly(new TierPlayerTo("1", "Busata"), new TierPlayerTo("2", "Hayabusa"));
        assertThat(service.suggestPlayers(link, "b").orElseThrow()).isEmpty();
        assertThat(service.suggestPlayers(UUID.randomUUID(), "bus")).isEmpty();
    }

    private List<String> labels(UUID link) {
        return service.get(link).orElseThrow().tiers().stream().map(TierTo::label).toList();
    }

    private void clear() {
        em.flush();
        em.clear();
    }
}
