package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubResults;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.domain.models.profile.Profile;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.ListHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubResultsMessageFactory {
    private final EAWRCFieldMapper fieldMapper;

    String entryTemplate = "${badgeRank} **${rank}** • ${flag} • **${displayName}** • ${time} *(${deltaTime}*)";

    public MessageEmbed createResultPost(ClubResults results, boolean requiresTracking) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        buildHeader(embedBuilder, results);
        buildEntries(embedBuilder, results, requiresTracking);
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
                ))
                .addField(new MessageEmbed.Field(
                        StringUtils.isBlank(results.championshipName()) ? "\u200E" : results.championshipName(),
                        "\u200E",
                        false
                ));
    }


    private void buildEntries(EmbedBuilder embedBuilder, ClubResults results, boolean requiresTracking) {
        int desiredGroupSize = 10;

        int totalEntries = results.entries().size();


        List<List<ClubLeaderboardEntry>> lists = ListHelpers.partitionInGroups(results.entries().stream()
                .filter(entry -> !requiresTracking || entry.isTracked() || entry.getRank() <= 10)
                .sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated)).limit(50).toList(), desiredGroupSize);

        for (int i = 0; i < lists.size(); i++) {
            List<ClubLeaderboardEntry> groupOfEntries = lists.get(i);
            String values = groupOfEntries.stream().map(entry -> {

                return StringSubstitutor.replace(entryTemplate, buildTemplateMap(entry, totalEntries));
            }).collect(Collectors.joining("\n"));

            embedBuilder.addField(new MessageEmbed.Field(buildResultsHeader(i, desiredGroupSize, requiresTracking).formatted(groupOfEntries.size()), values, false));

        }

    }

    @NotNull
    private static String buildResultsHeader(int idx, int groupSize, boolean requiresTracking) {
        if(idx == 0) {
            return "Top %s".formatted(groupSize);
        } else if(!requiresTracking){
            var startBound = (idx * groupSize) + 1;
            var endBound = (idx * groupSize) + groupSize;

            return "Top %s-%s".formatted(startBound,endBound);
        } else {
            return EmbedBuilder.ZERO_WIDTH_SPACE;
        }
    }


    private Map<String, String> buildTemplateMap(ClubLeaderboardEntry entry, int totalEntries) {
        Map<String, String> values = new HashMap<>();

        values.put("badgeRank", BadgeMapper.createBadge(entry.getRankAccumulated(), totalEntries));
        values.put("rank", String.valueOf(entry.getRankAccumulated()));
        values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityID(), FieldMappingType.EMOTE));
        values.put("displayName", entry.getDisplayName());
        values.put("time", DurationHelper.formatTime(entry.getTimeAccumulated()));
        values.put("deltaTime", DurationHelper.formatDelta(entry.getDifferenceAccumulated()));


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
