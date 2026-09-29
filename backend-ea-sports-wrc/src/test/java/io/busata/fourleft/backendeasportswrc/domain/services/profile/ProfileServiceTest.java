package io.busata.fourleft.backendeasportswrc.domain.services.profile;

import io.busata.fourleft.api.easportswrc.models.ProfileClaimState;
import io.busata.fourleft.api.easportswrc.models.ProfilePageTo;
import io.busata.fourleft.api.easportswrc.models.ProfileTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo.Outcome;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.projections.RacenetInfo;
import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.PeripheralType;
import io.busata.fourleft.common.Platform;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** Profile claims (V038) against real Postgres; the racenet lookup is stubbed. */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
@Import(ProfileService.class)
class ProfileServiceTest {

    @TestConfiguration
    static class Stubs {
        @Bean
        ConnectionFactory connectionFactory() {
            return Mockito.mock(ConnectionFactory.class);
        }

        @Bean
        ClubLeaderboardService clubLeaderboardService() {
            return Mockito.mock(ClubLeaderboardService.class);
        }
    }

    private static final RacenetInfo BUSATA = new RacenetInfo("1", "Busata", 3L);
    private static final RacenetInfo HAYABUSA = new RacenetInfo("2", "Hayabusa", 3L);
    private static final RacenetInfo BUSY = new RacenetInfo("3", "Busy", 3L);

    @Autowired
    ProfileService service;

    @Autowired
    ClubLeaderboardService leaderboards;

    @Autowired
    TestEntityManager em;

    @BeforeEach
    void racenet() {
        Mockito.reset(leaderboards);
        for (RacenetInfo info : new RacenetInfo[]{BUSATA, HAYABUSA, BUSY}) {
            when(leaderboards.findRacenet(info.racenet())).thenReturn(Optional.of(info));
            when(leaderboards.findRacenetBySsid(info.ssid())).thenReturn(Optional.of(info));
        }
    }

    @Test
    void firstClaimLinksTheAccount() {
        ProfileUpdateRequestResultTo result = service.requestUpdate("alice", "Busata");

        assertThat(result.outcome()).isEqualTo(Outcome.LINKED);
        assertThat(profile(result.requestId()).claimState()).isEqualTo(ProfileClaimState.CLAIMED);
        assertThat(profile(result.requestId()).racenet()).isEqualTo("Busata");
    }

    @Test
    void secondClaimantHasToConfirmOnThePage() {
        service.requestUpdate("alice", "Busata");

        ProfileUpdateRequestResultTo bobs = service.requestUpdate("bob", "Busata");

        assertThat(bobs.outcome()).isEqualTo(Outcome.CONFIRM_DISPUTE);
        assertThat(bobs.playerId()).isEqualTo("1");
        assertThat(bobs.racenet()).isEqualTo("Busata");
        // Nothing changed yet; the link is an unbound page to confirm on.
        assertThat(stored("1").getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
        assertThat(service.getPage(bobs.requestId())).get().extracting(ProfilePageTo::profile).isNull();
        assertThat(service.claimFromPage(bobs.requestId(), "1").outcome()).isEqualTo(Outcome.CONFIRM_DISPUTE);
        assertThat(stored("1").getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
    }

    @Test
    void confirmedDisputeKeepsTheHolder() {
        UUID alices = service.requestUpdate("alice", "Busata").requestId();
        UUID bobs = service.requestUpdate("bob", "Busata").requestId();

        assertThat(service.disputeFromPage(bobs, "1").outcome()).isEqualTo(Outcome.DISPUTED);

        Profile busata = stored("1");
        assertThat(busata.getDiscordId()).isEqualTo("alice");
        assertThat(busata.getClaimState()).isEqualTo(ProfileClaimState.DISPUTED);
        assertThat(busata.getDisputedByDiscordId()).isEqualTo("bob");
        assertThat(service.getPage(bobs)).get().extracting(ProfilePageTo::profile).isNull();
        // The holder can still edit, and disputing again changes nothing.
        assertThat(profile(alices).claimState()).isEqualTo(ProfileClaimState.DISPUTED);
        assertThat(service.requestUpdate("bob", "Busata").outcome()).isEqualTo(Outcome.DISPUTED);
    }

    @Test
    void oneOpenDisputePerUser() {
        service.requestUpdate("alice", "Busata");
        service.requestUpdate("carol", "Hayabusa");
        UUID bobs = service.requestUpdate("bob", null).requestId();
        service.disputeFromPage(bobs, "1");

        assertThat(service.requestUpdate("bob", "Hayabusa").outcome()).isEqualTo(Outcome.DISPUTE_LIMIT);
        assertThat(service.disputeFromPage(bobs, "2").outcome()).isEqualTo(Outcome.DISPUTE_LIMIT);
        assertThat(stored("2").getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
        // Free accounts can still be claimed.
        assertThat(service.requestUpdate("bob", "Busy").outcome()).isEqualTo(Outcome.LINKED);
    }

    @Test
    void oneDisputerPerAccount() {
        service.requestUpdate("alice", "Busata");
        service.disputeFromPage(service.requestUpdate("bob", null).requestId(), "1");

        UUID carols = service.requestUpdate("carol", null).requestId();

        assertThat(service.disputeFromPage(carols, "1").outcome()).isEqualTo(Outcome.ALREADY_DISPUTED);
        assertThat(stored("1").getDisputedByDiscordId()).isEqualTo("bob");
    }

    @Test
    void verifiedAccountsCantBeDisputed() {
        service.requestUpdate("alice", "Busata");
        em.getEntityManager().createNativeQuery("UPDATE profile SET claim_state = 'VERIFIED' WHERE id = '1'").executeUpdate();
        em.clear();
        UUID bobs = service.requestUpdate("bob", null).requestId();

        assertThat(service.requestUpdate("bob", "Busata").outcome()).isEqualTo(Outcome.VERIFIED_BY_OTHER);
        assertThat(service.disputeFromPage(bobs, "1").outcome()).isEqualTo(Outcome.VERIFIED_BY_OTHER);
        assertThat(stored("1").getClaimState()).isEqualTo(ProfileClaimState.VERIFIED);
    }

    @Test
    void disputeConfirmedAfterTheHolderLetGoLinksInstead() {
        service.requestUpdate("alice", "Busata");
        UUID bobs = service.requestUpdate("bob", "Busata").requestId();
        service.requestUpdate("alice", "Hayabusa");

        assertThat(service.disputeFromPage(bobs, "1").outcome()).isEqualTo(Outcome.LINKED);
        assertThat(profile(bobs).racenet()).isEqualTo("Busata");
    }

    @Test
    void claimingAnotherAccountReleasesThePreviousOne() {
        service.requestUpdate("alice", "Busata");
        service.disputeFromPage(service.requestUpdate("bob", "Busata").requestId(), "1");

        assertThat(service.requestUpdate("alice", "Hayabusa").outcome()).isEqualTo(Outcome.LINKED);
        em.flush();
        em.clear();

        Profile busata = stored("1");
        assertThat(busata.getDiscordId()).isNull();
        assertThat(busata.getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
        assertThat(busata.getDisputedByDiscordId()).isNull();
        // Up for grabs again.
        assertThat(service.requestUpdate("bob", "Busata").outcome()).isEqualTo(Outcome.LINKED);
    }

    @Test
    void withoutANameOpensTheHeldProfile() {
        service.requestUpdate("alice", "Busata");

        ProfileUpdateRequestResultTo result = service.requestUpdate("alice", null);

        assertThat(result.outcome()).isEqualTo(Outcome.LINKED);
        assertThat(profile(result.requestId()).id()).isEqualTo("1");
    }

    @Test
    void withoutANameOrProfileTheUserPicksOnThePage() {
        ProfileUpdateRequestResultTo result = service.requestUpdate("alice", " ");

        assertThat(result.outcome()).isEqualTo(Outcome.PICK);
        assertThat(service.getPage(result.requestId())).get().extracting(ProfilePageTo::profile).isNull();

        assertThat(service.claimFromPage(result.requestId(), "2").outcome()).isEqualTo(Outcome.LINKED);
        assertThat(profile(result.requestId()).racenet()).isEqualTo("Hayabusa");
    }

    @Test
    void unknownNameFallsBackToThePicker() {
        when(leaderboards.findRacenet("busata")).thenReturn(Optional.empty());

        assertThat(service.requestUpdate("alice", "busata").outcome()).isEqualTo(Outcome.PICK);
    }

    @Test
    void linksStopWorkingOnceTheProfileIsReleased() {
        UUID alices = service.requestUpdate("alice", "Busata").requestId();
        service.requestUpdate("alice", "Hayabusa");
        service.requestUpdate("bob", "Busata");

        assertThat(service.getPage(alices)).get().extracting(ProfilePageTo::profile).isNull();
        ProfileTo edit = new ProfileTo("1", "Hijacked", ControllerType.WHEEL, Platform.PC, PeripheralType.VR, "Busata", true, ProfileClaimState.CLAIMED);
        assertThatThrownBy(() -> service.updateProfile(alices, edit)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void disputerSeesAndWithdrawsTheirDispute() {
        service.requestUpdate("alice", "Busata");
        UUID bobs = service.requestUpdate("bob", null).requestId();
        service.disputeFromPage(bobs, "1");

        assertThat(service.getPage(bobs).orElseThrow().disputing().racenet()).isEqualTo("Busata");
        assertThat(stored("1").getDisputedAt()).isNotNull();

        ProfilePageTo page = service.withdrawDispute(bobs);

        assertThat(page.disputing()).isNull();
        Profile busata = stored("1");
        assertThat(busata.getDiscordId()).isEqualTo("alice");
        assertThat(busata.getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
        assertThat(busata.getDisputedByDiscordId()).isNull();
        // Free to dispute something else now.
        service.requestUpdate("carol", "Hayabusa");
        assertThat(service.disputeFromPage(bobs, "2").outcome()).isEqualTo(Outcome.DISPUTED);
    }

    @Test
    void dismissKeepsTheHolder() {
        service.requestUpdate("alice", "Busata");
        service.disputeFromPage(service.requestUpdate("bob", null).requestId(), "1");

        service.dismissDispute("1");

        assertThat(service.openDisputes()).isEmpty();
        assertThat(stored("1").getDiscordId()).isEqualTo("alice");
        assertThat(stored("1").getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
        assertThatThrownBy(() -> service.dismissDispute("1")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void transferHandsTheAccountToTheDisputer() {
        UUID alices = service.requestUpdate("alice", "Busata").requestId();
        service.requestUpdate("bob", "Busy");
        service.disputeFromPage(service.requestUpdate("bob", null).requestId(), "1");
        assertThat(service.openDisputes()).extracting(Profile::getId).containsExactly("1");

        service.transferToDisputer("1");
        em.flush();
        em.clear();

        Profile busata = stored("1");
        assertThat(busata.getDiscordId()).isEqualTo("bob");
        assertThat(busata.getClaimState()).isEqualTo(ProfileClaimState.CLAIMED);
        assertThat(busata.getDisputedByDiscordId()).isNull();
        assertThat(stored("3").getDiscordId()).isNull();
        assertThat(service.getPage(alices)).get().extracting(ProfilePageTo::profile).isNull();
        assertThat(service.requestUpdate("bob", null).racenet()).isEqualTo("Busata");
    }

    @Test
    void unknownLinksAreEmpty() {
        assertThat(service.getPage(UUID.randomUUID())).isEmpty();
        assertThat(service.requestExists(UUID.randomUUID())).isFalse();
    }

    private ProfileTo profile(UUID requestId) {
        return service.getPage(requestId).orElseThrow().profile();
    }

    private Profile stored(String ssid) {
        return service.getProfileById(ssid).orElseThrow();
    }
}
