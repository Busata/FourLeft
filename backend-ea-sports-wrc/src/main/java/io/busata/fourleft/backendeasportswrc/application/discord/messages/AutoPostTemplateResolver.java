package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.models.StageSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoPostTemplateResolver implements TemplateResolver<AutoPostMessageSummary, String> {
    private final EAWRCFieldMapper fieldMapper;

    @Override
    public String render(String template, AutoPostMessageSummary summary) {

        var messageTemplate = new MessageTemplate(template);

        EventSettings eventSettings = summary.event().getEventSettings();
        Stage lastStage = summary.event().getLastStage();
        StageSettings stageSettings = lastStage.getStageSettings();

        Map<String, String> values = new HashMap<>();
        values.put("eventCountryFlag", fieldMapper.getDiscordField("eventFlag#" + eventSettings.getLocationID(), FieldMappingType.EMOTE, eventSettings.getLocation()));
        values.put("lastStage", stageSettings.getRoute());
        values.put("eventVehicleClass", summary.vehicleClasses() != null ? summary.vehicleClasses() : eventSettings.getVehicleClass());
        values.put("totalEntries", String.valueOf(summary.totalEntries()));

        String entriesTemplate = messageTemplate.getReccuringTemplate("entries");
        // A mixed channel's entries need their class; custom templates that don't place it get it appended.
        if (summary.isMixed() && entriesTemplate != null && !entriesTemplate.contains("${class}")) {
            entriesTemplate = entriesTemplate + ClubResultsMessageFactory.CLASS_SUFFIX;
        }
        values.put("entries", this.resolveEntries(entriesTemplate, summary));

        return StringSubstitutor.replace(messageTemplate.getNormalizedTemplate(), values);
    }

    private String resolveEntries(String template, AutoPostMessageSummary summary) {
        Comparator<ClubLeaderboardEntry> order = summary.isMixed()
                ? Comparator.comparing(entry -> summary.mixed().get(entry).rank())
                : Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated);

        return summary.entries().stream().sorted(order).map(entry -> {
            Map<String, String> values = new HashMap<>();

            // A mixed channel ranks and gaps entries overall; a single board keeps racenet's.
            AutoPostMessageSummary.MixedEntry mixed = summary.mixed().get(entry);
            long rank = mixed != null ? mixed.rank() : entry.getRankAccumulated();
            Duration delta = mixed != null ? mixed.delta() : entry.getDifferenceAccumulated();

            values.put("badgeRank", BadgeMapper.createBadge(rank, summary.totalEntries(), entry.isDnf()));
            values.put("rank", String.valueOf(rank));
            values.put("class", mixed != null ? mixed.tag() : "");
            values.put("classRank", String.valueOf(entry.getRankAccumulated()));
            values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityID(), FieldMappingType.EMOTE, entry.getAlias()));
            values.put("displayName", entry.getAlias());
            values.put("totalTime", DurationHelper.formatTime(entry.getTimeAccumulated()));
            values.put("deltaTime", "(%s)".formatted(DurationHelper.formatDelta(delta)));
            values.put("vehicle", entry.getVehicle());
            
            if (entry.getDisplayName().equals("Qorsatevela")) {
                values.put("flag", ":flag_ge:");
            }
            
            if (entry.getDisplayName().equals("rjT36")) {
                values.put("flag", ":flag_sg:");
            }

            String playerPlatform = getPlayerPlatform(entry);

            values.put("platform", playerPlatform);

            String entryTemplate = StringSubstitutor.replace(template, values);

            log.debug("Entry template size: {} ", entryTemplate.length());

            return entryTemplate;
        }).collect(Collectors.joining("\n"));
    }

    private String getPlayerPlatform(ClubLeaderboardEntry entry) {
        return entry.getProfilePlatform()
                .map(profilePlatform -> fieldMapper.getDiscordField("platformEnum#" + profilePlatform.name(), FieldMappingType.EMOTE))
                .orElseGet(() -> fieldMapper.getDiscordField("platform#" + entry.getPlatform(), FieldMappingType.EMOTE, entry.getAlias()));
    }


}
