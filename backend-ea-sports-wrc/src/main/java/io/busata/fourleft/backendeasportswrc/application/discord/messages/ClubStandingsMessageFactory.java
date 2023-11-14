package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.ListHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubStandingsMessageFactory {
    private final EAWRCFieldMapper fieldMapper;

    String entryTemplate = "**${rank}**${deltaRank} • *${points}*${deltaPoints} • ${flag} • **${displayName}**";

    public MessageEmbed createStandingsPost(List<ChampionshipStanding> standings) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        buildHeader(embedBuilder);
        buildEntries(embedBuilder, standings);
        buildFooter(embedBuilder, standings);
        return embedBuilder.build();
    }


    private void buildHeader(EmbedBuilder embedBuilder) {
        embedBuilder.setTitle("**Championship standings**");
    }


    private void buildEntries(EmbedBuilder embedBuilder, List<ChampionshipStanding> standings) {
        var lists = ListHelpers.partitionInGroups(standings.stream().sorted(Comparator.comparing(ChampionshipStanding::getRank)).toList(), 10);

        lists.forEach(groupOfEntries -> {
            String values = groupOfEntries.stream().map(entry -> {
                return StringSubstitutor.replace(entryTemplate, buildTemplateMap(entry));
            }).collect(Collectors.joining("\n"));

            embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, values, false);

        });

    }


    private Map<String, String> buildTemplateMap(ChampionshipStanding entry) {
        Map<String, String> values = new HashMap<>();
        values.put("rank", String.valueOf(entry.getRank()));
        values.put("displayName", entry.getDisplayName());
        values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityId(), FieldMappingType.EMOTE));
        values.put("points", String.valueOf(entry.getPointsAccumulated()));

        values.put("deltaRank", createDeltaRank(entry));
        values.put("deltaPoints", createDeltaPoints(entry));

        return values;
    }

    private String createDeltaRank(ChampionshipStanding entry) {
        if(Objects.equals(entry.getRank(), entry.getPreviousRank())){
            return " **(+0)**";
        }
        return " **(%+d)**".formatted(entry.getRankDifference());
    }

    private String createDeltaPoints(ChampionshipStanding entry) {
        if(Objects.equals(entry.getRank(), entry.getPreviousRank())){
            return " *(+0)*";
        }
        return " *(%+d)*".formatted(entry.getPointsDifference());
    }


    private void buildFooter(EmbedBuilder embedBuilder, List<ChampionshipStanding> standings) {
        embedBuilder.addField(new MessageEmbed.Field(
                "Total entries",
                String.valueOf(standings.size()),
                false
        ));
    }
}
