package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
import io.busata.fourleft.backendeasportswrc.domain.models.StageSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping.FieldMappingType;
import io.busata.fourleft.backendeasportswrc.infrastructure.helpers.DurationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

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
        values.put("eventVehicleClass", eventSettings.getVehicleClass());
        values.put("totalEntries", String.valueOf(summary.totalEntries()));

        values.put("entries", this.resolveEntries(messageTemplate.getReccuringTemplate("entries"), summary));

        return StringSubstitutor.replace(messageTemplate.getNormalizedTemplate(), values);
    }

    private String resolveEntries(String template, AutoPostMessageSummary summary) {
        return summary.entries().stream().filter(entry -> {
            if (summary.totalEntries() <= 1000) {
                return true;
            } else {
                return entry.isTracked();
            }
        }).map(entry -> {
            Map<String, String> values = new HashMap<>();

            values.put("rankBadge", BadgeMapper.createBadge(entry.getRankAccumulated(), summary.totalEntries()));
            values.put("rank", String.valueOf(entry.getRank()));
            values.put("flag", fieldMapper.getDiscordField("nationalityFlag#" + entry.getNationalityID(), FieldMappingType.EMOTE, entry.getDisplayName()));
            values.put("displayName", entry.getDisplayName());
            values.put("totalTime", DurationHelper.formatTime(entry.getTimeAccumulated()));
            values.put("deltaTime", "(%s)".formatted(DurationHelper.formatDelta(entry.getDifferenceAccumulated())));
            values.put("vehicle", entry.getVehicle());
            values.put("platform", fieldMapper.getDiscordField("platform#" + entry.getPlatform(), FieldMappingType.EMOTE, entry.getDisplayName()));

            String entryTemplate = StringSubstitutor.replace(template, values);

            log.debug("Entry template size: {} ", entryTemplate.length());

            return entryTemplate;
        }).collect(Collectors.joining("\n"));
    }


}
