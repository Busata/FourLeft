package io.busata.fourleft.backendeasportswrc.application.discord.results;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchorEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.scoring.ScoringAnchors;
import io.busata.fourleft.common.ScoringStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-data check: recompute club 146's live championship standings through the ACTUAL
 * {@link ClubResultsService} twice — once under the old LOOKUP_TABLE, once under POINT_ANCHOR — and diff
 * the top 50. Runs against a local Postgres loaded from a prod dump (profile "realdata") + a local
 * RabbitMQ, so it exercises the real scoring path on real data. Gated on RUN_REALDATA=1 so it never runs
 * in CI / a normal build (there's no prod dump there).
 */
@SpringBootTest
@ActiveProfiles("realdata")
@EnabledIfEnvironmentVariable(named = "RUN_REALDATA", matches = "1")
class ClubScoringTop50RealDataTest {

    private static final int TOP_N = 50;

    @Autowired ClubResultsService clubResultsService;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    private static ScoringAnchors club146Anchors() {
        return new ScoringAnchors(1, List.of(
                new ScoringAnchorEntry(1, 2500, null),
                new ScoringAnchorEntry(2, 2200, null),
                new ScoringAnchorEntry(3, null, new BigDecimal("100")),
                new ScoringAnchorEntry(4, 2025, null),
                new ScoringAnchorEntry(5, null, new BigDecimal("25")),
                new ScoringAnchorEntry(7, null, new BigDecimal("15")),
                new ScoringAnchorEntry(10, null, new BigDecimal("5")),
                new ScoringAnchorEntry(26, null, new BigDecimal("2")),
                new ScoringAnchorEntry(204, null, new BigDecimal("1.83")),
                new ScoringAnchorEntry(788, 424, null),
                new ScoringAnchorEntry(789, null, new BigDecimal("1.99"))));
    }

    @Test
    void top50IsStableBetweenLookupAndPointAnchor() throws Exception {
        // The real 1000-row lookup table, straight from the dumped club-146 config.
        String tableJson = jdbc.queryForObject(
                "select scoring_table::text from discord_club_configuration " +
                        "where club_id = '146' and scoring_table is not null limit 1", String.class);
        Map<String, Integer> oldTable = mapper.readValue(tableJson, new TypeReference<>() {});

        DiscordClubConfiguration config = new DiscordClubConfiguration(0L, 0L, "146", true);
        config.setCustomScoringEnabled(true);
        config.setScoringTable(oldTable);
        config.setScoringAnchors(club146Anchors());

        config.setScoringStrategy(ScoringStrategy.LOOKUP_TABLE);
        List<ChampionshipStanding> lookup = clubResultsService.getStandings(config);

        config.setScoringStrategy(ScoringStrategy.POINT_ANCHOR);
        List<ChampionshipStanding> anchor = clubResultsService.getStandings(config);

        assertThat(lookup).as("lookup standings").isNotEmpty();
        assertThat(anchor).as("anchor standings").isNotEmpty();

        System.out.println("\n==================== CLUB 146 STANDINGS COMPARISON ====================");
        System.out.printf("players: lookup=%d anchor=%d%n", lookup.size(), anchor.size());

        List<ChampionshipStanding> lTop = lookup.stream().limit(TOP_N).toList();
        List<ChampionshipStanding> aTop = anchor.stream().limit(TOP_N).toList();

        // Ordered ssid sequences of the two top-50s.
        List<String> lSeq = lTop.stream().map(ChampionshipStanding::getSsid).toList();
        List<String> aSeq = aTop.stream().map(ChampionshipStanding::getSsid).toList();

        System.out.printf("%n%-4s | %-26s | %10s | %10s | %6s%n", "rank", "player (lookup)", "pts(look)", "pts(anch)", "Δpts");
        System.out.println("-".repeat(70));
        Map<String, Integer> anchorPtsBySsid = anchor.stream()
                .collect(java.util.stream.Collectors.toMap(ChampionshipStanding::getSsid,
                        ChampionshipStanding::getPointsAccumulated, (a, b) -> a));
        for (int i = 0; i < lTop.size(); i++) {
            ChampionshipStanding s = lTop.get(i);
            Integer aPts = anchorPtsBySsid.get(s.getSsid());
            String name = s.getDisplayName() == null ? "?" : s.getDisplayName();
            System.out.printf("%-4d | %-26s | %10d | %10s | %6s%n",
                    i + 1, name.length() > 26 ? name.substring(0, 26) : name,
                    s.getPointsAccumulated(),
                    aPts == null ? "-" : aPts,
                    aPts == null ? "-" : (aPts - s.getPointsAccumulated()));
        }

        boolean sameOrder = lSeq.equals(aSeq);
        System.out.println("-".repeat(70));
        System.out.println("TOP-50 ORDER IDENTICAL: " + sameOrder);
        if (!sameOrder) {
            for (int i = 0; i < TOP_N; i++) {
                String l = i < lSeq.size() ? lSeq.get(i) : "-";
                String a = i < aSeq.size() ? aSeq.get(i) : "-";
                if (!l.equals(a)) {
                    System.out.printf("  rank %d: lookup=%s  anchor=%s%n", i + 1,
                            nameOf(lTop, l), nameOf(aTop, a));
                }
            }
        }
        System.out.println("=======================================================================\n");
    }

    private static String nameOf(List<ChampionshipStanding> list, String ssid) {
        return list.stream().filter(s -> s.getSsid().equals(ssid)).findFirst()
                .map(ChampionshipStanding::getDisplayName).orElse(ssid);
    }
}
