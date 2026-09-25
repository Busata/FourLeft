package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService.StandingsSection;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ChampionshipStanding;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
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

    private static final int MAX_ENTRIES = 50;
    private static final int GROUP_SIZE = 10;
    // Footer: one "Total entries" field.
    private static final int FOOTER_RESERVED_LENGTH = 50;
    private static final int FOOTER_RESERVED_FIELDS = 1;

    public MessageEmbed createStandingsPost(List<ChampionshipStanding> standings, boolean requiresTracking) {
        return createSectionedStandingsPost(List.of(new StandingsSection(null, standings)), requiresTracking);
    }

    /**
     * One post for all sections; a MIXED channel has a titled section per class. The 50-entry cap is shared
     * between sections so each class gets its top.
     */
    public MessageEmbed createSectionedStandingsPost(List<StandingsSection> sections, boolean requiresTracking) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        buildHeader(embedBuilder);

        int perSection = sections.isEmpty() ? MAX_ENTRIES : Math.max(GROUP_SIZE, MAX_ENTRIES / sections.size());
        for (int i = 0; i < sections.size(); i++) {
            StandingsSection section = sections.get(i);
            // Later sections still need room for their title and a first block.
            int laterSections = sections.size() - i - 1;
            buildEntries(embedBuilder, section, requiresTracking, perSection,
                    FOOTER_RESERVED_LENGTH + laterSections * 200, FOOTER_RESERVED_FIELDS + laterSections);
        }

        buildFooter(embedBuilder, sections.stream().mapToInt(section -> section.standings().size()).sum());
        return embedBuilder.build();
    }


    private void buildHeader(EmbedBuilder embedBuilder) {
        embedBuilder.setTitle("**Championship standings**");
    }


    private void buildEntries(EmbedBuilder embedBuilder, StandingsSection section, boolean requiresTracking, int limit,
                              int reservedLength, int reservedFields) {
        List<String> lines = section.standings().stream()
                .filter(entry -> !requiresTracking || entry.isTracked() || entry.getRank() <= 20)
                .filter(entry -> entry.getPointsAccumulated() > 0)
                .limit(limit)
                .sorted(Comparator.comparing(ChampionshipStanding::getRank))
                .map(entry -> StringSubstitutor.replace(entryTemplate, buildTemplateMap(entry)))
                .toList();

        String title = section.title() == null ? null : "**%s**".formatted(EmbedBudget.truncate(section.title(), MessageEmbed.TITLE_MAX_LENGTH - 4));
        EmbedBudget.addEntryFields(embedBuilder, lines, GROUP_SIZE, title, reservedLength, reservedFields);
    }


    private Map<String, String> buildTemplateMap(ChampionshipStanding entry) {
        Map<String, String> values = new HashMap<>();
        values.put("rank", String.valueOf(entry.getRank()));
        values.put("displayName", entry.getDisplayName());
        values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityId(), FieldMappingType.EMOTE));
        values.put("points", String.valueOf(entry.getPointsAccumulated()));

        values.put("deltaRank", createDeltaRank(entry));
        values.put("deltaPoints", createDeltaPoints(entry));

        if (entry.getDisplayName().equals("Qorsatevela")) {
            values.put("flag", ":flag_ge:");
        }

        if (entry.getDisplayName().equals("rjT36")) {
            values.put("flag", ":flag_sg:");
        }
        
        return values;
    }

    private String createDeltaRank(ChampionshipStanding entry) {
        if(entry.isNewEntry()) {
            return " **(new)**";
        }
        if(Objects.equals(entry.getRank(), entry.getPreviousRank())){
            return " **(+0)**";
        }
        return " **(%+d)**".formatted(entry.getRankDifference());
    }

    private String createDeltaPoints(ChampionshipStanding entry) {
        if(Objects.equals(entry.getPointsAccumulated(), entry.getPointsAccumulatedPrevious())){
            return " *(+0)*";
        }
        return " *(%+d)*".formatted(entry.getPointsDifference());
    }


    private void buildFooter(EmbedBuilder embedBuilder, int totalEntries) {
        embedBuilder.addField(new MessageEmbed.Field(
                "Total entries",
                String.valueOf(totalEntries),
                false
        ));
    }
}
