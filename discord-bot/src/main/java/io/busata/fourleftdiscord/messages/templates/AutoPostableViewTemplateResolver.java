package io.busata.fourleftdiscord.messages.templates;

import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.tiers.VehicleTo;
import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.domain.players.ControllerType;
import io.busata.fourleft.domain.players.Platform;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostResultList;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableView;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AutoPostableViewTemplateResolver implements TemplateResolver<AutoPostableView> {
    private final DR2FieldMapper fieldMapper;

    @Override
    public String resolve(MessageTemplate template, AutoPostableView value) {
        return StringSubstitutor.replace(template.getTemplate(), buildValuesMap(template, value)).trim();
    }

    public Map<String, String> buildValuesMap(MessageTemplate template, AutoPostableView value) {
        Map<String, String> valueMap = new HashMap<>();

        final var eventInfos = value.getMultiListResults().stream().map(AutoPostResultList::eventInfoTo).collect(Collectors.toList());

        String country = eventInfos.stream().map(EventInfoTo::country).distinct().map(fieldMapper::createEmoticon).collect(Collectors.joining(" "));
        String vehicleClass = eventInfos.stream().map(EventInfoTo::vehicleClass).distinct().map(fieldMapper::createHumanReadable).collect(Collectors.joining(" "));

        String stageName = eventInfos.stream().flatMap((EventInfoTo eventInfoTo) -> eventInfoTo.stageNames().stream()).distinct().sorted(Comparator.reverseOrder()).limit(1).collect(Collectors.joining(" • "));

        valueMap.put("countryEmoticon", country);
        valueMap.put("stageNames", stageName);
        valueMap.put("vehicleClassName", vehicleClass);
        valueMap.put("singleList","");
        valueMap.put("multiList","");

        if(value.getMultiListResults().size() == 1) {
            final var entriesTemplate = template.getRecurringTemplate("entries");
            String entries = value.getMultiListResults().stream()
                    .flatMap(list -> {
                        return list.results().stream().map(resultEntryTo -> {
                            return buildEntry(list, resultEntryTo, entriesTemplate);
                        });
                    }).collect(Collectors.joining("\n"));

            valueMap.put("singleList", entries);

        } else {
            String multiList = value.getMultiListResults().stream()
                    .filter(list -> list.results().size() > 0)
                    .map(list -> buildMultiList(template, list))
                    .collect(Collectors.joining("\n"));
            valueMap.put("multiList", multiList);
        }

        return valueMap;
    }

    private String buildMultiList(MessageTemplate template, AutoPostResultList list) {
        final var entriesTemplate = template.getRecurringTemplate("entries");
        final var groupedListTemplate = template.getRecurringTemplate("multiList");

        final var listEntries = list.results().stream().map(resultEntryTo -> {
            return buildEntry(list, resultEntryTo, entriesTemplate);
        }).collect(Collectors.joining("\n"));

        Map<String, String> groupedListValueMap = new HashMap<>();
        groupedListValueMap.put("entries", listEntries);

        groupedListValueMap.put("vehicleRestrictions", "");
        if (list.restrictions() instanceof ResultListRestrictionsTo restrictions) {
            String vehicleRestrictions = restrictions.getAllowedVehicles().stream().map(VehicleTo::displayName).collect(Collectors.joining(" • "));
            groupedListValueMap.put("vehicleRestrictions", "*(%s)*".formatted(vehicleRestrictions));
        }
        groupedListValueMap.put("tierName", list.name());

        return StringSubstitutor.replace(groupedListTemplate, groupedListValueMap);
    }

    public String buildEntry(AutoPostResultList listData, ResultEntryTo entry, String template) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("rank", String.valueOf(entry.rank()));
        valueMap.put("badgeRank", BadgeMapper.createRankBasedIcon(entry.rank(), entry.isDnf()));
        valueMap.put("nationalityEmoticon", fieldMapper.createEmoticon(entry.nationality()));
        valueMap.put("name", entry.name());
        valueMap.put("totalTime", entry.totalTime());
        valueMap.put("totalDiff", entry.totalDiff());
        valueMap.put("vehicleName", entry.vehicle());
        valueMap.put("powerStageBadge", determinePowerstageBadge(entry));
        valueMap.put("tierName", listData.name());

        valueMap.put("platformInfo", "");

        /*
        if(entry.platform() != Platform.UNKNOWN && entry.controllerType() != ControllerType.UNKNOWN) {
            valueMap.put("platformInfo", " %s • %s •".formatted(
                    fieldMapper.createEmoticon(entry.platform().name()),
                    fieldMapper.createEmoticon(entry.controllerType().name())
            ));
        }
         */

        valueMap.put("validVehicle", "");

        if (listData.restrictions() instanceof ResultListRestrictionsTo restrictions) {
            boolean useValidVehicle = restrictions.isValidVehicle(entry.vehicle());
            valueMap.put("validVehicle", useValidVehicle ? "" : ":pineapple:");
        }

        return StringSubstitutor.replace(template, valueMap);
    }

    private String determinePowerstageBadge(ResultEntryTo entry) {
        if (entry.stageRank() > 5) {
            return "";
        }

        return ":rocket: (*%s*) • ".formatted(entry.stageRank());

    }
}
