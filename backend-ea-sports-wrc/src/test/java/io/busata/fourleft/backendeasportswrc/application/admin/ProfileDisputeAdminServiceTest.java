package io.busata.fourleft.backendeasportswrc.application.admin;

import io.busata.fourleft.api.easportswrc.models.DiscordUserInfoTo;
import io.busata.fourleft.api.easportswrc.models.ProfileDisputeAdminTo;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.projections.RacenetInfo;
import io.busata.fourleft.backendeasportswrc.domain.services.profile.ProfileService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordUserDirectory;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** The disputes operator page (V039 admin links) against real Postgres; discord lookups are stubbed. */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
@Import({ProfileDisputeAdminService.class, ProfileService.class})
class ProfileDisputeAdminServiceTest {

    @TestConfiguration
    static class Stubs {
        @Bean
        ConnectionFactory connectionFactory() {
            return Mockito.mock(ConnectionFactory.class);
        }

        @Bean
        ClubLeaderboardService clubLeaderboardService() {
            ClubLeaderboardService leaderboards = Mockito.mock(ClubLeaderboardService.class);
            RacenetInfo busata = new RacenetInfo("1", "Busata", 3L);
            when(leaderboards.findRacenet("Busata")).thenReturn(Optional.of(busata));
            when(leaderboards.findRacenetBySsid("1")).thenReturn(Optional.of(busata));
            return leaderboards;
        }

        @Bean
        DiscordUserDirectory discordUserDirectory() {
            DiscordUserDirectory directory = Mockito.mock(DiscordUserDirectory.class);
            when(directory.lookup(anyString())).thenAnswer(call -> new DiscordUserInfoTo(call.getArgument(0), "user-" + call.getArgument(0), null, null));
            return directory;
        }
    }

    @Autowired
    ProfileDisputeAdminService admin;

    @Autowired
    ProfileService profiles;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    TestEntityManager em;

    @Test
    void listsAndResolvesOpenDisputes() {
        profiles.requestUpdate("alice", "Busata");
        profiles.disputeFromPage(profiles.requestUpdate("bob", null).requestId(), "1");
        UUID link = admin.newLink("test");

        List<ProfileDisputeAdminTo> disputes = admin.openDisputes(link);

        assertThat(disputes).singleElement().satisfies(dispute -> {
            assertThat(dispute.racenet()).isEqualTo("Busata");
            assertThat(dispute.holder().username()).isEqualTo("user-alice");
            assertThat(dispute.disputer().id()).isEqualTo("bob");
            assertThat(dispute.disputedAt()).isNotNull();
        });
        assertThat(admin.transfer(link, "1")).isEmpty();
        assertThat(profiles.getProfileById("1").orElseThrow().getDiscordId()).isEqualTo("bob");
    }

    @Test
    void unknownOrExpiredLinksAreRefused() {
        UUID link = admin.newLink("test");
        jdbc.update("UPDATE admin_link SET expires_at = ? WHERE id = ?", ApplicationClock.now().minusMinutes(1), link);
        em.clear();

        assertThatThrownBy(() -> admin.openDisputes(link)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> admin.openDisputes(UUID.randomUUID())).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> admin.dismiss(UUID.randomUUID(), "1")).isInstanceOf(ResponseStatusException.class);
    }
}
