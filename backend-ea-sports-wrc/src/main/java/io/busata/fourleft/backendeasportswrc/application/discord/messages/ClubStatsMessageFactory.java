package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.CarStatistics;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ClubStats;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubStatsMessageFactory {
    private final EAWRCFieldMapper fieldMapper;

    public MessageEmbed createPost(ClubStats results, DiscordClubConfiguration discordClubConfiguration) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            buildHeader(embedBuilder, results);
            buildEntries(embedBuilder, results);
            return embedBuilder.build();
    }

    private void buildEntries(EmbedBuilder embedBuilder, ClubStats results) {
        CarStatistics carStatistics = results.carStatistics();

        long totalEntries = carStatistics.carEntries().values().stream().mapToLong(x -> x).sum();

        embedBuilder.addField(new MessageEmbed.Field(
                "Car statistics *(%s entries)*".formatted(totalEntries),
                buildCarStatistics(results),
                false
        ));

        embedBuilder.addField(new MessageEmbed.Field(
                "Player statistics",
                buildPlayerStatistics(results), 
         false
         ));

    }

    private String buildPlayerStatistics(ClubStats results) {
        return "%s\n%s\n%s".formatted(
                "**Total DNF** • *%s*".formatted(results.playerStatistics().totalDnf()),
                "**Percentage finished** • *%s%%*".formatted(results.playerStatistics().percentageFinished()),
                "**Percentage DNF** • *%s%%*".formatted(results.playerStatistics().percentageDnf())
        );
    }

private String buildCarStatistics(ClubStats results) {
        CarStatistics statistics = results.carStatistics();

        List<String> lines = statistics.carPercentages()
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> {

                    Double percentage = entry.getValue();
                    Long entries = statistics.carEntries().getOrDefault(entry.getKey(), 0L);
                    Long topEntries = statistics.carTopEntries().getOrDefault(entry.getKey(), 0L);

                    String topEntriesString = topEntries > 0 ? "• *%s in top 10* •".formatted(topEntries) : "•";

                    return String.format("**%.1f%%** • *%s entries* %s **%s**", percentage, entries, topEntriesString, entry.getKey());
                }).toList();

        // A mixed channel's two classes can list more cars than one field holds; keep the most used ones.
        StringBuilder value = new StringBuilder();
        int shown = 0;
        for (String line : lines) {
            int remaining = lines.size() - shown - 1;
            int reserve = remaining > 0 ? 24 : 0;
            if (value.length() + line.length() + 1 + reserve > MessageEmbed.VALUE_MAX_LENGTH) {
                break;
            }
            if (!value.isEmpty()) {
                value.append('\n');
            }
            value.append(line);
            shown++;
        }
        if (shown < lines.size()) {
            value.append('\n').append(EmbedBudget.MORE_TEMPLATE.formatted(lines.size() - shown));
        }
        return value.toString();
    }

    private void buildHeader(EmbedBuilder embedBuilder, ClubStats results) {
        embedBuilder.setTitle("**Event statistics**")
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
    }
}
