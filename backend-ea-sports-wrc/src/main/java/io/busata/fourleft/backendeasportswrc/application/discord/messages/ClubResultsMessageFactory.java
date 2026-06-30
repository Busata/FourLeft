package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.WeatherMappings;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import io.busata.fourleft.common.BadgeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubResultsMessageFactory {
    private final EAWRCFieldMapper fieldMapper;

    public static String defaultTemplate = "**${rank}** • ${flag} • **${displayName}** • ${time} *(${deltaTime}*)";

    public MessageEmbed createResultPost(ClubResults results, DiscordClubConfiguration configuration) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        buildHeader(embedBuilder, results);
        buildEntries(embedBuilder, results, configuration.getResultsEntryTemplate(), configuration.isRequiresTracking());
        buildFooter(embedBuilder, results);
        return embedBuilder.build();
    }


    private void buildHeader(EmbedBuilder embedBuilder, ClubResults results) {
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

        boolean singleStageEvent = results.stages().size() == 1;

        if (singleStageEvent) {
            embedBuilder.addField(new MessageEmbed.Field(
                    " ",
                    " ",
                    false
            ));
        }
        
        embedBuilder.addField(new MessageEmbed.Field(
                "**Club board**",
                "[Link](%s)".formatted(buildRacenetLink(results)),
                singleStageEvent
        ));

        if (singleStageEvent) {
            embedBuilder.addField(new MessageEmbed.Field(
                    "**TT board**",
                    "[Link](%s)".formatted(buildTTBoardLink(results)),
                    true
            ));
        }
    }

    private String buildRacenetLink(ClubResults results) {
        return "https://racenet.com/ea_sports_wrc/clubs/%s".formatted(results.clubId());
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


    private static final int MAX_FIELD_VALUE_LENGTH = MessageEmbed.VALUE_MAX_LENGTH;
    private static final int DESIRED_GROUP_SIZE = 10;

    private void buildEntries(EmbedBuilder embedBuilder, ClubResults results, String entryTemplate, boolean requiresTracking) {
        int totalEntries = results.entries().size();

        List<String> renderedEntries = results.entries().stream()
                .filter(entry -> !requiresTracking || entry.isTracked() || entry.getRank() <= 10)
                .sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated))
                .limit(50)
                .map(entry -> StringSubstitutor.replace(entryTemplate, buildTemplateMap(entry, totalEntries)))
                .toList();

        StringBuilder currentField = new StringBuilder();
        int currentGroupSize = 0;

        for (String renderedEntry : renderedEntries) {
            int additionalLength = currentField.isEmpty() ? renderedEntry.length() : renderedEntry.length() + 1;

            boolean exceedsLength = currentField.length() + additionalLength > MAX_FIELD_VALUE_LENGTH;
            boolean exceedsGroupSize = currentGroupSize >= DESIRED_GROUP_SIZE;

            if (currentGroupSize > 0 && (exceedsLength || exceedsGroupSize)) {
                embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, currentField.toString(), false);
                currentField.setLength(0);
                currentGroupSize = 0;
            }

            if (!currentField.isEmpty()) {
                currentField.append("\n");
            }
            currentField.append(renderedEntry);
            currentGroupSize++;
        }

        if (currentGroupSize > 0) {
            embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, currentField.toString(), false);
        }
    }

    private Map<String, String> buildTemplateMap(ClubLeaderboardEntry entry, int totalEntries) {
        Map<String, String> values = new HashMap<>();

        values.put("badgeRank", BadgeMapper.createBadge(entry.getRankAccumulated(), totalEntries, entry.isDnf()));
        values.put("rank", String.valueOf(entry.getRankAccumulated()));
        values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityID(), FieldMappingType.EMOTE));
        values.put("displayName", entry.getAlias());
        values.put("time", DurationHelper.formatTime(entry.getTimeAccumulated()));
        values.put("deltaTime", DurationHelper.formatDelta(entry.getDifferenceAccumulated()));
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
