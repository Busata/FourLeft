package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.common.RestrictionDisplayMode;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.RestrictionType;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pins the restriction rendering in Discord result posts: WARN appends a marker after template
 * substitution (so custom entry templates keep working), display-EXCLUDE drops violators and
 * re-ranks the remaining entries, and an active rule is announced in the header.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClubResultsMessageFactoryRestrictionTest {

    private static final String EVENT_ID = "event-1";

    @Mock EAWRCFieldMapper fieldMapper;

    private ClubResultsMessageFactory factory;
    private DiscordClubConfiguration configuration;

    @BeforeEach
    void setUp() {
        factory = new ClubResultsMessageFactory(fieldMapper, new RestrictionService());
        configuration = new DiscordClubConfiguration(0L, 0L, "club-1", true);
        when(fieldMapper.getDiscordField(any(), any())).thenReturn("");
        when(fieldMapper.getDiscordField(any(), any(), any())).thenReturn("");
    }

    private void givenRestriction(RestrictionDisplayMode displayMode) {
        configuration.setEventRestrictions(List.of(new EventRestriction(
                RestrictionType.VEHICLE_ALLOWLIST, null, EVENT_ID,
                displayMode, RestrictionScoringMode.EXCLUDE, null, List.of("Audi Sport quattro S1 E2"))));
    }

    private static ClubResults results(ClubLeaderboardEntry... entries) {
        return new ClubResults(
                "club-1", "champ-1", EVENT_ID, "Test club",
                "Finland", 1L, 1L,
                "Group B", 1L,
                "Summer", 1L,
                "Dry", 1L,
                LocalDateTime.now(),
                ZonedDateTime.now(),
                List.of("Stage 1", "Stage 2"),
                List.of(entries));
    }

    private static ClubLeaderboardEntry entry(String name, long rankAccumulated, String vehicle) {
        Duration time = Duration.ofMinutes(3).plusSeconds(rankAccumulated);
        return ClubLeaderboardEntry.builder()
                .displayName(name)
                .ssid(name)
                .rank(rankAccumulated)
                .rankAccumulated(rankAccumulated)
                .nationalityID(1L)
                .vehicle(vehicle)
                .time(time)
                .timeAccumulated(time)
                .timePenalty(Duration.ZERO)
                .differenceAccumulated(Duration.ZERO)
                .build();
    }

    private static String renderedEntries(MessageEmbed embed) {
        return embed.getFields().stream()
                .filter(field -> Objects.equals(field.getName(), net.dv8tion.jda.api.EmbedBuilder.ZERO_WIDTH_SPACE))
                .map(MessageEmbed.Field::getValue)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void warnModeMarksViolatorsAndKeepsRanks() {
        givenRestriction(RestrictionDisplayMode.WARN);
        MessageEmbed embed = factory.createResultPost(results(
                entry("violator", 1, "Lancia Delta S4"),
                entry("second", 2, "Audi Sport quattro S1 E2")), configuration);

        String entries = renderedEntries(embed);
        assertThat(entries).contains("**1** • ", "violator", "⚠️");
        assertThat(entries.lines().filter(line -> line.contains("second")).findFirst().orElseThrow())
                .contains("**2**")
                .doesNotContain("⚠️");
    }

    @Test
    void excludeModeDropsViolatorsAndReRanks() {
        givenRestriction(RestrictionDisplayMode.EXCLUDE);
        MessageEmbed embed = factory.createResultPost(results(
                entry("violator", 1, "Lancia Delta S4"),
                entry("second", 2, "Audi Sport quattro S1 E2")), configuration);

        String entries = renderedEntries(embed);
        assertThat(entries).doesNotContain("violator");
        // The surviving driver is promoted to displayed rank 1.
        assertThat(entries).contains("**1** • ");
        assertThat(entries).doesNotContain("**2**");
    }

    @Test
    void activeRestrictionIsAnnouncedInTheHeader() {
        givenRestriction(RestrictionDisplayMode.EXCLUDE);
        MessageEmbed embed = factory.createResultPost(results(
                entry("second", 2, "Audi Sport quattro S1 E2")), configuration);

        MessageEmbed.Field restrictionField = embed.getFields().stream()
                .filter(field -> Objects.equals(field.getName(), "**Restriction**"))
                .findFirst()
                .orElseThrow();
        assertThat(restrictionField.getValue()).contains("Audi Sport quattro S1 E2", "violators hidden");
    }

    @Test
    void withoutARuleNothingChanges() {
        MessageEmbed embed = factory.createResultPost(results(
                entry("first", 1, "Lancia Delta S4"),
                entry("second", 2, "Audi Sport quattro S1 E2")), configuration);

        assertThat(embed.getFields().stream().map(MessageEmbed.Field::getName))
                .doesNotContain("**Restriction**");
        String entries = renderedEntries(embed);
        assertThat(entries).contains("**1** • ", "**2** • ").doesNotContain("⚠️");
    }
}
