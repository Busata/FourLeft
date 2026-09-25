package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.AutopostingEntryService;
import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary.MixedEntry;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelClass;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService.StandingsSection;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.discord.results.MergedRanking;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.models.StageSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.common.RestrictionDisplayMode;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.RestrictionType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A MIXED channel's merged posts: one overall classification with class tags, restrictions only on the
 * primary club's entries, no time trial link, standings per class in one post, and every post staying
 * inside Discord's size limits once the tags make entries longer.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MixedChannelPostsTest {

    @Mock EAWRCFieldMapper fieldMapper;

    private ClubResultsMessageFactory resultsFactory;
    private DiscordClubConfiguration configuration;

    @BeforeEach
    void setUp() {
        resultsFactory = new ClubResultsMessageFactory(fieldMapper, new RestrictionService());
        configuration = new DiscordClubConfiguration(0L, 0L, "wrc", true);
        when(fieldMapper.getDiscordField(any(), any())).thenReturn(":flag_be:");
        when(fieldMapper.getDiscordField(any(), any(), any())).thenReturn(":flag_be:");
    }

    @Test
    void mergedRankingIsOverallByAccumulatedTimeLikeRacenet() {
        ClubLeaderboardEntry wrcWinner = entry("a", 1, 100, false);
        ClubLeaderboardEntry wrc2Winner = entry("b", 1, 90, false);
        // Racenet places a DNF by its penalty-inflated time, not below every finisher — so does the merge.
        ClubLeaderboardEntry wrc2Dnf = entry("c", 2, 300, true);
        ClubLeaderboardEntry wrcSlow = entry("d", 2, 400, false);

        MergedRanking ranking = MergedRanking.of(List.of(wrcWinner, wrcSlow, wrc2Dnf, wrc2Winner));

        assertThat(ranking.entries()).containsExactly(wrc2Winner, wrcWinner, wrc2Dnf, wrcSlow);
        assertThat(ranking.deltaOf(wrcWinner)).isEqualTo(Duration.ofSeconds(10));
        assertThat(ranking.deltaOf(wrc2Dnf)).isEqualTo(Duration.ofSeconds(210));
        assertThat(ranking.rankOf(wrcSlow)).isEqualTo(4);
    }

    @Test
    void resultsAreOneClassificationWithClassTags() {
        ClubLeaderboardEntry wrc = entry("wrcDriver", 1, 100, false);
        ClubLeaderboardEntry wrc2 = entry("wrc2Driver", 1, 90, false);

        MessageEmbed embed = resultsFactory.createResultPost(mixedResults(List.of(wrc), List.of(wrc2)), configuration);

        List<String> lines = entryLines(embed);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).startsWith("**1**").contains("wrc2Driver", "*WRC2*");
        assertThat(lines.get(1)).startsWith("**2**").contains("wrcDriver", "*WRC*", "+00:10");
        assertThat(field(embed, "**Car**")).isEqualTo("WRC / WRC2");
        assertThat(field(embed, "**Club boards**")).contains("clubs/wrc)", "clubs/wrc2)");
        assertThat(embed.getFields()).noneMatch(f -> Objects.equals(f.getName(), "**TT board**"));
    }

    @Test
    void customTemplatePlacingTheClassIsLeftAlone() {
        configuration = new DiscordClubConfiguration(0L, 0L, "wrc", true) {
            @Override
            public String getResultsEntryTemplate() {
                return "${rank} [${class}] ${displayName}";
            }
        };

        MessageEmbed embed = resultsFactory.createResultPost(mixedResults(List.of(entry("x", 1, 100, false)), List.of()), configuration);

        assertThat(entryLines(embed)).containsExactly("1 [WRC] x");
    }

    @Test
    void restrictionsOnlyApplyToThePrimaryClubsEntries() {
        configuration.setEventRestrictions(List.of(new EventRestriction(
                RestrictionType.VEHICLE_ALLOWLIST, null, "event-wrc",
                RestrictionDisplayMode.WARN, RestrictionScoringMode.EXCLUDE, null, List.of("Allowed car"))));

        MessageEmbed embed = resultsFactory.createResultPost(
                mixedResults(List.of(entry("wrcDriver", 1, 100, false)), List.of(entry("wrc2Driver", 1, 90, false))), configuration);

        List<String> lines = entryLines(embed);
        assertThat(lines.stream().filter(line -> line.contains("wrcDriver")).findFirst().orElseThrow()).contains("⚠️");
        assertThat(lines.stream().filter(line -> line.contains("wrc2Driver")).findFirst().orElseThrow()).doesNotContain("⚠️");
    }

    @Test
    void longEntriesStayWithinDiscordLimits() {
        List<ClubLeaderboardEntry> wrc = new ArrayList<>();
        List<ClubLeaderboardEntry> wrc2 = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            wrc.add(entry("A very long display name number " + i + " ".repeat(10), i, 100 + i, false));
            wrc2.add(entry("Another very long display name " + i + " ".repeat(10), i, 100 + i, false));
        }
        configuration = new DiscordClubConfiguration(0L, 0L, "wrc", true) {
            @Override
            public String getResultsEntryTemplate() {
                return ClubResultsMessageFactory.defaultTemplate + " • " + "padding ".repeat(12);
            }
        };

        // build() throws when an embed breaks a limit, so reaching the assertions is most of the test.
        MessageEmbed embed = resultsFactory.createResultPost(mixedResults(wrc, wrc2), configuration);

        assertThat(embed.getLength()).isLessThanOrEqualTo(MessageEmbed.EMBED_MAX_LENGTH_BOT);
        assertThat(embed.getFields()).hasSizeLessThanOrEqualTo(MessageEmbed.MAX_FIELD_AMOUNT);
        assertThat(embed.getFields()).allMatch(f -> f.getValue().length() <= MessageEmbed.VALUE_MAX_LENGTH);
        assertThat(String.join("\n", entryLines(embed))).contains("more*");
        assertThat(field(embed, "**Event ending**")).isNotNull();
    }

    @Test
    void shortEntriesKeepTenPerField() {
        List<ClubLeaderboardEntry> entries = IntStream.rangeClosed(1, 25).mapToObj(i -> entry("d" + i, i, 100 + i, false)).toList();

        MessageEmbed embed = resultsFactory.createResultPost(singleResults(entries), configuration);

        assertThat(embed.getFields().stream().filter(f -> Objects.equals(f.getName(), EmbedBuilder.ZERO_WIDTH_SPACE))
                .map(f -> f.getValue().lines().count()).toList()).containsExactly(10L, 10L, 5L);
    }

    @Test
    void standingsGetATitledSectionPerClass() {
        ClubStandingsMessageFactory factory = new ClubStandingsMessageFactory(fieldMapper);

        MessageEmbed embed = factory.createSectionedStandingsPost(List.of(
                new StandingsSection("WRC", List.of(standing("a", 1, 50), standing("b", 2, 40))),
                new StandingsSection("WRC2", List.of(standing("c", 1, 45)))), false);

        assertThat(embed.getFields()).extracting(MessageEmbed.Field::getName).containsExactly("**WRC**", "**WRC2**", "Total entries");
        assertThat(field(embed, "Total entries")).isEqualTo("3");
    }

    @Test
    void autopostEntriesCarryOverallRankAndClass() {
        AutoPostTemplateResolver resolver = new AutoPostTemplateResolver(fieldMapper);
        ClubLeaderboardEntry wrc = entry("wrcDriver", 1, 100, false);
        ClubLeaderboardEntry wrc2 = entry("wrc2Driver", 1, 90, false);
        Map<ClubLeaderboardEntry, MixedEntry> mixed = new IdentityHashMap<>();
        mixed.put(wrc2, new MixedEntry("event-wrc2", "WRC2", 1, Duration.ZERO));
        mixed.put(wrc, new MixedEntry("event-wrc", "WRC", 2, Duration.ofSeconds(10)));

        String message = resolver.render(AutoPostMessageService.defaultTemplate,
                new AutoPostMessageSummary(event(), 2, List.of(wrc, wrc2), mixed, "WRC / WRC2"));

        List<String> lines = message.lines().toList();
        assertThat(lines.get(0)).contains("**WRC / WRC2**", "2 entries");
        assertThat(lines.get(1)).startsWith("**1**").contains("wrc2Driver").endsWith("*WRC2*");
        assertThat(lines.get(2)).startsWith("**2**").contains("wrcDriver", "+00:10").endsWith("*WRC*");
    }

    @Test
    void autopostDropsLowestRanksUntilTheMessageFits() {
        AutoPostTemplateResolver resolver = new AutoPostTemplateResolver(fieldMapper);
        AutoPostMessageService service = new AutoPostMessageService(mock(DiscordGateway.class), mock(AutopostingEntryService.class),
                mock(DiscordClubConfigurationService.class), resolver);

        List<ClubLeaderboardEntry> entries = new ArrayList<>();
        Map<ClubLeaderboardEntry, MixedEntry> mixed = new IdentityHashMap<>();
        for (int i = 1; i <= 10; i++) {
            ClubLeaderboardEntry entry = entry("Driver " + i + " " + "x".repeat(150), i, 100 + i, false);
            entries.add(entry);
            mixed.put(entry, new MixedEntry("event-wrc", "World Rally Car 1997", i, Duration.ofSeconds(i)));
        }
        AutoPostMessageSummary summary = new AutoPostMessageSummary(event(), 10, entries, mixed, "WRC / WRC2");

        AutoPostMessageSummary fitted = service.fit(AutoPostMessageService.defaultTemplate, summary);

        assertThat(resolver.render(AutoPostMessageService.defaultTemplate, fitted).length()).isLessThan(AutoPostMessageService.MAX_MESSAGE_LENGTH);
        assertThat(fitted.entries()).isNotEmpty().hasSizeLessThan(10).containsExactlyElementsOf(entries.subList(0, fitted.entries().size()));
    }

    private ClubResults mixedResults(List<ClubLeaderboardEntry> wrc, List<ClubLeaderboardEntry> wrc2) {
        List<ClubLeaderboardEntry> entries = new ArrayList<>();
        Map<ClubLeaderboardEntry, String> entryClubs = new IdentityHashMap<>();
        wrc.forEach(e -> { entries.add(e); entryClubs.put(e, "wrc"); });
        wrc2.forEach(e -> { entries.add(e); entryClubs.put(e, "wrc2"); });
        return new ClubResults("wrc", "champ-wrc", "event-wrc", "Championship", "Finland", 1L, 1L,
                "WRC / WRC2", 1L, "Summer", 1L, "Dry", 1L, LocalDateTime.now(), ZonedDateTime.now(),
                List.of("Stage 1", "Stage 2"), entries,
                List.of(ChannelClass.of("wrc", null, "WRC"), ChannelClass.of("wrc2", "WRC2", "WRC2")), entryClubs);
    }

    private ClubResults singleResults(List<ClubLeaderboardEntry> entries) {
        return new ClubResults("wrc", "champ-wrc", "event-wrc", "Championship", "Finland", 1L, 1L,
                "WRC", 1L, "Summer", 1L, "Dry", 1L, LocalDateTime.now(), ZonedDateTime.now(),
                List.of("Stage 1", "Stage 2"), entries);
    }

    private static Event event() {
        Event event = new Event("event-wrc", "board", ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(1), 2L,
                new EventSettings(1L, "WRC", 1L, "Summer", 1L, "Finland", ""));
        event.updateStages(List.of(new Stage("s1", "board", new StageSettings(1L, "Ouninpohja", 1L, "Dry", 1L, "Day", 1L, "None"))));
        return event;
    }

    private static ClubLeaderboardEntry entry(String name, long rankAccumulated, long seconds, boolean dnf) {
        Duration time = Duration.ofSeconds(seconds);
        return ClubLeaderboardEntry.builder()
                .displayName(name)
                .ssid(name)
                .rank(rankAccumulated)
                .rankAccumulated(rankAccumulated)
                .nationalityID(1L)
                .platform(1L)
                .vehicle("Car")
                .time(time)
                .timeAccumulated(time)
                .timePenalty(dnf ? time : Duration.ZERO)
                .differenceAccumulated(Duration.ZERO)
                .build();
    }

    private static ChampionshipStanding standing(String name, int rank, int points) {
        ChampionshipStanding standing = new ChampionshipStanding(UUID.randomUUID(), name, name, points, rank, 1);
        return standing;
    }

    private static List<String> entryLines(MessageEmbed embed) {
        return embed.getFields().stream()
                .filter(f -> Objects.equals(f.getName(), EmbedBuilder.ZERO_WIDTH_SPACE))
                .flatMap(f -> f.getValue().lines())
                .collect(Collectors.toList());
    }

    private static String field(MessageEmbed embed, String name) {
        return embed.getFields().stream().filter(f -> Objects.equals(f.getName(), name)).map(MessageEmbed.Field::getValue).findFirst().orElse(null);
    }
}
