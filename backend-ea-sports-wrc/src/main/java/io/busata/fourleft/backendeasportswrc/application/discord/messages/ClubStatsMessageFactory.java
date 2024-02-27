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

import java.util.Comparator;
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

        embedBuilder.addField(new MessageEmbed.Field(
                "Car statistics",
                buildCarStatistics(results),
                false
        ));

    }

    private String buildCarStatistics(ClubStats results) {
        CarStatistics statistics = results.carStatistics();

        return statistics.carPercentages()
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> {

                    Double percentage = entry.getValue();
                    Long entries = statistics.carEntries().getOrDefault(entry.getKey(), 0L);
                    Long topEntries = statistics.carTopEntries().getOrDefault(entry.getKey(), 0L);

                    String topEntriesString = topEntries > 0 ? "• %s in top 10 •".formatted(topEntries) : "•";

                    return String.format("%.1f • *%s entries* %s **%s**", percentage, entries, topEntriesString, entry.getKey());


        }).collect(Collectors.joining("\n"));
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
