package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialLeaderboardEntryRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the time-trial "target times" embed: it only fires for single-stage events, addresses the
 * board by the {@code location-route-surface-vehicleClass} tuple, and renders the stored top rows
 * with a delta on everyone but the leader.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TimeTrialTopMessageFactoryTest {

    @Mock EAWRCFieldMapper fieldMapper;
    @Mock TimeTrialLeaderboardEntryRepository entryRepository;

    private TimeTrialTopMessageFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TimeTrialTopMessageFactory(fieldMapper, entryRepository);
        when(fieldMapper.getDiscordField(any(), any())).thenReturn("");
    }

    private static ClubResults results(List<String> stages) {
        return new ClubResults(
                "club-1", "champ-1", "event-1", "Test club",
                "Portugal", 6L, 101L,
                "WRC", 19L,
                "Summer", 1L,
                "Clear (Dry)", 1L,
                LocalDateTime.now(),
                ZonedDateTime.now(),
                stages,
                List.of());
    }

    private static TimeTrialLeaderboardEntry entry(long rank, String name, String time, String diff) {
        return TimeTrialLeaderboardEntry.builder()
                .combinationId("6-101-0-19")
                .displayName(name)
                .rank(rank)
                .nationalityID(1L)
                .time(time)
                .differenceToFirst(diff)
                .build();
    }

    private static String renderedEntries(MessageEmbed embed) {
        return embed.getFields().stream()
                .filter(field -> Objects.equals(field.getName(), EmbedBuilder.ZERO_WIDTH_SPACE))
                .map(MessageEmbed.Field::getValue)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void rendersTopEntriesForTheEventsBoard() {
        Page<TimeTrialLeaderboardEntry> page = new PageImpl<>(List.of(
                entry(1, "leader", "03:12.100", null),
                entry(2, "chaser", "03:13.500", "00:01.400")));
        // The dry WRC board for Fridão (location 6, route 101, surface 0, class 19).
        when(entryRepository.findLatestPage(eq("6-101-0-19"), any(Pageable.class))).thenReturn(page);

        MessageEmbed embed = factory.createTopPost(results(List.of("Fridão")), false).orElseThrow();

        String entries = renderedEntries(embed);
        assertThat(entries)
                .contains("**1** • ", "leader", "03:12.100")
                .contains("**2** • ", "chaser", "03:13.500", "+00:01.400");
        // Leader carries no delta.
        assertThat(entries.lines().filter(line -> line.contains("leader")).findFirst().orElseThrow())
                .doesNotContain("+");
    }

    @Test
    void multiStageEventProducesNoPost() {
        assertThat(factory.createTopPost(results(List.of("Stage 1", "Stage 2")), false)).isEmpty();
    }

    @Test
    void emptyBoardProducesNoPost() {
        when(entryRepository.findLatestPage(any(), any(Pageable.class))).thenReturn(Page.empty());

        assertThat(factory.createTopPost(results(List.of("Fridão")), false)).isEmpty();
    }

    @Test
    void trackedOnlyUsesTheTrackedQueryAndKeepsBoardWideRanks() {
        Page<TimeTrialLeaderboardEntry> page = new PageImpl<>(List.of(
                entry(4, "trackedPlayer", "03:15.000", "00:02.900")));
        when(entryRepository.findLatestTrackedPage(eq("6-101-0-19"), any(Pageable.class))).thenReturn(page);

        MessageEmbed embed = factory.createTopPost(results(List.of("Fridão")), true).orElseThrow();

        // The tracked filter happens in the query; the rendered row keeps its global rank and delta.
        assertThat(renderedEntries(embed)).contains("**4** • ", "trackedPlayer", "03:15.000", "+00:02.900");
        verify(entryRepository, never()).findLatestPage(any(), any(Pageable.class));
    }

    @Test
    void trackedOnlyWithNoTrackedEntriesProducesNoPost() {
        when(entryRepository.findLatestTrackedPage(any(), any(Pageable.class))).thenReturn(Page.empty());

        assertThat(factory.createTopPost(results(List.of("Fridão")), true)).isEmpty();
    }
}
