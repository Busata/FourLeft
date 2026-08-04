package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.WeatherMappings;
import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialLeaderboardEntryRepository;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builds the "Time Trial top 10" embed that rides along with a new-event post so members see the
 * target times to beat. The board is addressed by the same {@code location-route-surface-vehicleClass}
 * tuple as {@link TimeTrialLeaderboardEntry}'s combination, derived from the event's last stage.
 *
 * <p>Only single-stage events map cleanly to one TT board (mirroring the "TT board" link in
 * {@link ClubResultsMessageFactory}); multi-stage events and boards with no stored rows yield
 * {@link Optional#empty()} so nothing is posted.
 *
 * <p>With {@code trackedOnly}, only discord-tracked players are listed — they keep their board-wide
 * rank and delta to the global leader, since the point is target times against the full board.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrialTopMessageFactory {

    private final EAWRCFieldMapper fieldMapper;
    private final TimeTrialLeaderboardEntryRepository entryRepository;

    private static final int TOP_N = 10;
    private static final String entryTemplate = "**${rank}** • ${flag} • **${displayName}** • ${time}${delta}";

    public Optional<MessageEmbed> createTopPost(ClubResults results, boolean trackedOnly) {
        // A TT board is per-stage; only a single-stage event maps to exactly one board.
        if (results.stages().size() != 1) {
            log.info("TT top post • club {} • skipped, event has {} stages", results.clubId(), results.stages().size());
            return Optional.empty();
        }

        String combinationId = buildCombinationId(results);

        PageRequest topPage = PageRequest.of(0, TOP_N, Sort.by(Sort.Direction.ASC, "rank"));
        List<TimeTrialLeaderboardEntry> top = (trackedOnly
                ? entryRepository.findLatestTrackedPage(combinationId, topPage)
                : entryRepository.findLatestPage(combinationId, topPage))
                .getContent();

        if (top.isEmpty()) {
            // Board not synced yet or gone from Racenet — no target times to show.
            log.info("TT top post • club {} • skipped, board {} has no {}entries", results.clubId(), combinationId,
                    trackedOnly ? "tracked " : "");
            return Optional.empty();
        }

        log.info("TT top post • club {} • posting top {} of board {}", results.clubId(), top.size(), combinationId);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("**Time Trial • Target times**");
        embedBuilder.addField(new MessageEmbed.Field(
                "**Board**",
                "[Link](%s)".formatted(buildTTBoardLink(results)),
                false
        ));

        String rendered = top.stream()
                .map(this::renderEntry)
                .collect(Collectors.joining("\n"));
        embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, rendered, false);

        return Optional.of(embedBuilder.build());
    }

    private String renderEntry(TimeTrialLeaderboardEntry entry) {
        Map<String, String> values = new HashMap<>();
        values.put("rank", String.valueOf(entry.getRank()));
        values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityID(), FieldMappingType.EMOTE));
        values.put("displayName", entry.getDisplayName());
        values.put("time", formatTime(entry.getTime()));

        String diff = entry.getDifferenceToFirst();
        boolean isLeader = entry.getRank() != null && entry.getRank() == 1L;
        values.put("delta", (isLeader || diff == null || diff.isBlank()) ? "" : " *(%s)*".formatted(formatDelta(diff)));

        return StringSubstitutor.replace(entryTemplate, values);
    }

    // Racenet stores raw "00:04:37.5470000" strings; render them in the same style as the club
    // results embeds. An unparseable value falls back to the raw string rather than dropping the row.
    private String formatTime(String rawTime) {
        if (rawTime == null || rawTime.isBlank()) {
            return "-";
        }
        try {
            return DurationHelper.formatTime(DurationHelper.parseDuration(rawTime));
        } catch (RuntimeException ex) {
            return rawTime;
        }
    }

    private String formatDelta(String rawDiff) {
        try {
            return DurationHelper.formatDelta(DurationHelper.parseDuration(rawDiff));
        } catch (RuntimeException ex) {
            return "+" + rawDiff;
        }
    }

    private int surfaceCondition(ClubResults results) {
        return WeatherMappings.isDry(results.lastStageWeatherAndSurface()) ? 0 : 1;
    }

    private String buildCombinationId(ClubResults results) {
        return "%s-%s-%s-%s".formatted(
                results.locationID(),
                results.lastStageRouteID(),
                surfaceCondition(results),
                results.vehicleClassID());
    }

    private String buildTTBoardLink(ClubResults results) {
        return "https://racenet.com/ea_sports_wrc/leaderboards/?selectedLocation=%s&selectedRoute=%s&selectedSurfaceCondition=%s&selectedVehicleClass=%s"
                .formatted(
                        results.locationID(),
                        results.lastStageRouteID(),
                        surfaceCondition(results),
                        results.vehicleClassID());
    }
}
