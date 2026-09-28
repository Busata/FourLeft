package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelClass;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.discord.results.MergedRanking;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.WeatherMappings;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.backendeasportswrc.domain.services.restrictions.RestrictionService;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import io.busata.fourleft.common.BadgeType;
import io.busata.fourleft.common.RestrictionDisplayMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubResultsMessageFactory {
    private final EAWRCFieldMapper fieldMapper;
    private final RestrictionService restrictionService;

    public static String defaultTemplate = "**${rank}** • ${flag} • **${displayName}** • ${time} *(${deltaTime}*)";

    // Appended to a MIXED channel's entry template when it doesn't place ${class} itself.
    static final String CLASS_SUFFIX = " • *${class}*";

    // Footer: three inline fields of a few dozen characters each.
    private static final int FOOTER_RESERVED_LENGTH = 200;
    private static final int FOOTER_RESERVED_FIELDS = 3;

    public MessageEmbed createResultPost(ClubResults results, DiscordClubConfiguration configuration) {
        Map<String, EventRestriction> restrictions = resolveRestrictions(results, configuration);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        buildHeader(embedBuilder, results, restrictions);
        buildEntries(embedBuilder, results, configuration.getResultsEntryTemplate(), configuration.isRequiresTracking(), restrictions);
        buildFooter(embedBuilder, results);
        return embedBuilder.build();
    }

    /**
     * The rule for each club on the board, keyed by club id; clubs without one are absent. Rules target a
     * club's own championship/event ids, so in a MIXED channel every class is resolved against its own
     * matched event.
     */
    private Map<String, EventRestriction> resolveRestrictions(ClubResults results, DiscordClubConfiguration configuration) {
        Map<String, EventRestriction> restrictions = new LinkedHashMap<>();
        if (!results.isMixed()) {
            restrictionService.resolveRestriction(configuration, results.championshipId(), results.eventId())
                    .ifPresent(rule -> restrictions.put(results.clubId(), rule));
            return restrictions;
        }
        for (ChannelClass channelClass : results.classes()) {
            boolean primary = channelClass.clubId().equals(results.clubId());
            String championshipId = channelClass.championshipId() != null || !primary ? channelClass.championshipId() : results.championshipId();
            String eventId = channelClass.eventId() != null || !primary ? channelClass.eventId() : results.eventId();
            restrictionService.resolveRestriction(configuration, championshipId, eventId)
                    .ifPresent(rule -> restrictions.put(channelClass.clubId(), rule));
        }
        return restrictions;
    }


    private void buildHeader(EmbedBuilder embedBuilder, ClubResults results, Map<String, EventRestriction> restrictions) {
        embedBuilder.setTitle("**Results**")
                .addField(new MessageEmbed.Field(
                        "**Country**",
                        "%s %s".formatted(fieldMapper.getDiscordField("eventFlag#" + results.locationID(), FieldMappingType.EMOTE, results.location()), results.location()),
                        true
                ))
                .addField(new MessageEmbed.Field(
                        "**Car**",
                        results.vehicleClass(),
                        true
                ))
                .addField(new MessageEmbed.Field(
                        "**Stages**",
                        String.join(", ", results.stages()),
                        true
                ));

        if (!restrictions.isEmpty()) {
            // A MIXED channel lists each restricted class on its own line.
            String permitted = results.isMixed()
                    ? results.classes().stream()
                            .filter(c -> restrictions.containsKey(c.clubId()))
                            .map(c -> "*%s*: %s".formatted(c.tag(), permittedCars(restrictions.get(c.clubId()))))
                            .collect(Collectors.joining("\n"))
                    : permittedCars(restrictions.get(results.clubId()));
            embedBuilder.addField(new MessageEmbed.Field(
                    "**Permitted cars**",
                    EmbedBudget.truncate(permitted, MessageEmbed.VALUE_MAX_LENGTH),
                    false
            ));
        }

        boolean singleStageEvent = results.stages().size() == 1;

        if (singleStageEvent) {
            embedBuilder.addField(new MessageEmbed.Field(
                    " ",
                    " ",
                    false
            ));
        }
        
        embedBuilder.addField(new MessageEmbed.Field(
                results.isMixed() ? "**Club boards**" : "**Club board**",
                results.isMixed()
                        ? results.classes().stream().map(c -> "[%s](%s)".formatted(c.tag(), buildRacenetLink(c.clubId()))).collect(Collectors.joining(" • "))
                        : "[Link](%s)".formatted(buildRacenetLink(results.clubId())),
                singleStageEvent
        ));

        // Time trial boards are per car class; a mixed channel has several, so it links none.
        if (singleStageEvent && !results.isMixed()) {
            embedBuilder.addField(new MessageEmbed.Field(
                    "**TT board**",
                    "[Link](%s)".formatted(buildTTBoardLink(results)),
                    true
            ));
        }
    }

    private static String permittedCars(EventRestriction rule) {
        String suffix = rule.displayMode() == RestrictionDisplayMode.EXCLUDE ? " *(violators hidden)*" : "";
        return String.join(", ", Optional.ofNullable(rule.allowedVehicles()).orElse(List.of())) + suffix;
    }

    private String buildRacenetLink(String clubId) {
        return "https://racenet.com/ea_sports_wrc/clubs/%s".formatted(clubId);
    }

    private String buildTTBoardLink(ClubResults results) {

        Long locationId = results.locationID();
        Long routeId = results.lastStageRouteID();
        Long vehicleClassId = results.vehicleClassID();
        int surfaceCondition = WeatherMappings.isDry(results.lastStageWeatherAndSurface()) ? 0 : 1;

        return "https://racenet.com/ea_sports_wrc/leaderboards/?selectedLocation=%s&selectedRoute=%s&selectedSurfaceCondition=%s&selectedVehicleClass=%s"
                .formatted(
                        locationId,
                        routeId,
                        surfaceCondition,
                        vehicleClassId
                );
    }


    private static final int DESIRED_GROUP_SIZE = 10;

    private void buildEntries(EmbedBuilder embedBuilder, ClubResults results, String entryTemplate, boolean requiresTracking, Map<String, EventRestriction> restrictions) {
        // Footer/badge totals stay at the racenet board size, also under display-EXCLUDE.
        int totalEntries = results.entries().size();

        // Display modes are per rule, so in a MIXED channel one class can hide violators while another warns.
        boolean excludeViolators = restrictions.values().stream().anyMatch(rule -> rule.displayMode() == RestrictionDisplayMode.EXCLUDE);

        List<ClubLeaderboardEntry> visibleEntries = results.entries().stream()
                .filter(entry -> !violates(results, restrictions, entry, RestrictionDisplayMode.EXCLUDE))
                .toList();

        // A single club's board keeps racenet's rank and gap; merged boards are re-ranked overall. Under
        // display-EXCLUDE the remaining entries are re-ranked 1..n either way.
        Map<ClubLeaderboardEntry, Long> displayRanks = new HashMap<>();
        Map<ClubLeaderboardEntry, Duration> displayDeltas = new HashMap<>();
        List<ClubLeaderboardEntry> boardEntries;
        if (results.isMixed()) {
            MergedRanking ranking = MergedRanking.of(visibleEntries);
            boardEntries = ranking.entries();
            boardEntries.forEach(entry -> {
                displayRanks.put(entry, ranking.rankOf(entry));
                displayDeltas.put(entry, ranking.deltaOf(entry));
            });
        } else {
            boardEntries = visibleEntries.stream().sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated)).toList();
            for (int i = 0; i < boardEntries.size(); i++) {
                ClubLeaderboardEntry entry = boardEntries.get(i);
                displayRanks.put(entry, excludeViolators ? i + 1 : entry.getRankAccumulated());
                displayDeltas.put(entry, entry.getDifferenceAccumulated());
            }
        }

        String template = results.isMixed() && !entryTemplate.contains("${class}") ? entryTemplate + CLASS_SUFFIX : entryTemplate;

        List<String> renderedEntries = boardEntries.stream()
                .filter(entry -> !requiresTracking || entry.isTracked() || (results.isMixed() ? displayRanks.get(entry) : entry.getRank()) <= 10)
                .limit(50)
                .map(entry -> {
                    Map<String, String> values = buildTemplateMap(entry, displayRanks.get(entry), displayDeltas.get(entry), totalEntries);
                    values.put("class", Optional.ofNullable(results.tagOf(entry)).orElse(""));
                    values.put("classRank", String.valueOf(entry.getRankAccumulated()));
                    String rendered = StringSubstitutor.replace(template, values);
                    // Appended after template substitution so custom entry templates keep working.
                    String home = results.offClassHomes().get(entry);
                    if (violates(results, restrictions, entry, RestrictionDisplayMode.WARN) || home != null) {
                        rendered += " ⚠️";
                    }
                    if (home != null) {
                        rendered += " *(home: %s)*".formatted(home);
                    }
                    return rendered;
                })
                .toList();

        EmbedBudget.addEntryFields(embedBuilder, renderedEntries, DESIRED_GROUP_SIZE, null, FOOTER_RESERVED_LENGTH, FOOTER_RESERVED_FIELDS);
    }

    /** Whether the entry breaks its own club's rule, and that rule uses the given display mode. */
    private boolean violates(ClubResults results, Map<String, EventRestriction> restrictions, ClubLeaderboardEntry entry, RestrictionDisplayMode displayMode) {
        EventRestriction rule = restrictions.get(results.clubOf(entry));
        return rule != null && rule.displayMode() == displayMode && restrictionService.violates(rule, entry);
    }

    private Map<String, String> buildTemplateMap(ClubLeaderboardEntry entry, Long displayRank, Duration delta, int totalEntries) {
        Map<String, String> values = new HashMap<>();

        values.put("badgeRank", BadgeMapper.createBadge(displayRank, totalEntries, entry.isDnf()));
        values.put("rank", String.valueOf(displayRank));
        values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityID(), FieldMappingType.EMOTE));
        values.put("displayName", entry.getAlias());
        values.put("time", DurationHelper.formatTime(entry.getTimeAccumulated()));
        values.put("deltaTime", DurationHelper.formatDelta(delta));
        if (entry.getDisplayName().equals("Qorsatevela")) {
            values.put("flag", ":flag_ge:");
        }

        if (entry.getDisplayName().equals("rjT36")) {
            values.put("flag", ":flag_sg:");
        }

        return values;
    }


    private void buildFooter(EmbedBuilder embedBuilder, ClubResults results) {

        embedBuilder.addField(new MessageEmbed.Field(
                "**Last Update**",
                "<t:%s:R>".formatted(results.lastUpdated().toEpochSecond(ZoneOffset.UTC)),
                true
        ));

        if (!results.entries().isEmpty()) {
            embedBuilder.addField(new MessageEmbed.Field(
                    "**Total entries**",
                    String.valueOf(results.entries().size()),
                    true
            ));
        } else {
            embedBuilder.addBlankField(true);
        }
        embedBuilder.addField(new MessageEmbed.Field(
                "**Event ending**",
                "<t:%s:R>".formatted(results.eventCloseDate().toEpochSecond()),
                true
        ));
    }
}
