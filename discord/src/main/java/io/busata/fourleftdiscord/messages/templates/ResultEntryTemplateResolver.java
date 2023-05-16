package io.busata.fourleftdiscord.messages.templates;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.VehicleEntryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import io.busata.fourleftdiscord.messages.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ResultEntryTemplateResolver implements TemplateResolver<DriverEntryTo> {
    private final DR2FieldMapper fieldMapper;

    @Override
    public String resolve(MessageTemplate template, DriverEntryTo value) {
        return StringSubstitutor.replace(template.getTemplate(), buildValuesMap(value));
    }

    public Map<String, String> buildValuesMap(DriverEntryTo entry) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("rank",String.valueOf(entry.activityRank()));
        valueMap.put("badgeRank", BadgeMapper.createRankBasedIcon(entry.activityRank(), entry.isDnf()));
        valueMap.put("percentageRank", BadgeMapper.createPercentageBasedIcon(entry.percentageRank(), entry.isDnf()));
        valueMap.put("nationalityEmoticon",fieldMapper.createEmoticon(entry.nationality()));
        valueMap.put("vehicle",entry.vehicles().stream().map(VehicleEntryTo::vehicleName).collect(Collectors.joining(",")));
        valueMap.put("name",entry.racenet());
        valueMap.put("totalTime",entry.activityTotalTime());
        valueMap.put("totalDiff",entry.activityTotalDiff());
        valueMap.put("platform", fieldMapper.createEmoticon(entry.platform().platform().name()));
        valueMap.put("controllerType", fieldMapper.createEmoticon(entry.platform().controller().name()));

        return valueMap;
    }
}
