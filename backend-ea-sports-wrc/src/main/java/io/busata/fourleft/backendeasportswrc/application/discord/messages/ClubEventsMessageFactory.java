package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.fieldmapping.EAWRCFieldMapper;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.Stage;
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
public class ClubEventsMessageFactory {
    private final EAWRCFieldMapper fieldMapper;

    String singleEventSingleStageFormat = "${eventFlag} • **${carClass}** • ${stageName} *(${season} - ${weather})*";

    String multipleEventsEventFormat = "${eventFlag} • **${carClass}** • ${season}";
    String multipleEventsStageFormat = "${stageName}${service} • ${weather}";


    public MessageEmbed createEventSummary(Championship activeChampionship) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        buildHeader(embedBuilder);
        buildEntries(embedBuilder, activeChampionship, true);
        return embedBuilder.build();
    }

    public MessageEmbed createEventMinimalSummary(Championship activeChampionship) {
        EmbedBuilder builder = new EmbedBuilder();
        buildHeader(builder);
        buildEntries(builder, activeChampionship, false);

        return builder.build();
    }

    private void buildHeader(EmbedBuilder embedBuilder) {
        embedBuilder.setTitle("**Championship summary**");
    }

    private void buildEntries(EmbedBuilder embedBuilder, Championship activeChampionship, boolean includeStages) {

        activeChampionship.getEvents().forEach(event -> {
            if(event.getStages().size() > 1) {
                var eventHeader = StringSubstitutor.replace(multipleEventsEventFormat, buildEventMap(event));
                String stages;
                if(includeStages) {
                     stages = event.getStages().stream().map(stage -> {
                        return StringSubstitutor.replace(multipleEventsStageFormat, buildStageTemplateMap(stage));
                    }).collect(Collectors.joining("\n"));
                } else {
                    stages = EmbedBuilder.ZERO_WIDTH_SPACE;
                }
                embedBuilder.addField(eventHeader, stages, false);
            } else {
                var values =  StringSubstitutor.replace(singleEventSingleStageFormat, buildTemplateMap(event));
                embedBuilder.addField(EmbedBuilder.ZERO_WIDTH_SPACE, values, false);
            }

        });
    }

    private Map<String, String> buildTemplateMap(Event entry) {

        var eventSettings = entry.getEventSettings();

        var stage = entry.getStages().get(entry.getStages().size()- 1);
        var stageSettings = stage.getStageSettings();

        Map<String, String> values = new HashMap<>();

        values.put("eventFlag", fieldMapper.getDiscordField("eventFlag#" + eventSettings.getLocationID(), FieldMappingType.EMOTE, eventSettings.getLocation()));
        values.put("carClass", eventSettings.getVehicleClass());
        values.put("stageName", stageSettings.getRoute());
        values.put("season", eventSettings.getWeatherSeason());
        values.put("weather", stageSettings.getWeatherAndSurface());

        return values;
    }

    private Map<String, String> buildEventMap(Event entry) {

        var eventSettings = entry.getEventSettings();

        Map<String, String> values = new HashMap<>();

        values.put("eventFlag", fieldMapper.getDiscordField("eventFlag#" + eventSettings.getLocationID(), FieldMappingType.EMOTE));
        values.put("carClass", eventSettings.getVehicleClass());
        values.put("season", eventSettings.getWeatherSeason());

        return values;
    }


    private Map<String, String> buildStageTemplateMap(Stage stage) {

        var stageSettings = stage.getStageSettings();

        Map<String, String> values = new HashMap<>();

        values.put("stageName", stageSettings.getRoute());
        values.put("weather", stageSettings.getWeatherAndSurface());
        values.put("timeOfDay", stageSettings.getTimeOfDay());
        String serviceAre = stageSettings.getServiceAre();
        if(!Objects.equals(serviceAre, "None")) {
            values.put("service", " • :wrench:");
        } else {
            values.put("service", "");

        }
        return values;
    }


}
