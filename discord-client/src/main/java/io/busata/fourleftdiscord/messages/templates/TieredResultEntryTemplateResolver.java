package io.busata.fourleftdiscord.messages.templates;

import io.busata.fourleft.api.models.tiers.TieredResultEntryTo;
import io.busata.fourleftdiscord.fieldmapper.DR2FieldMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class TieredResultEntryTemplateResolver implements TemplateResolver<TieredResultEntryTo> {
    private final DR2FieldMapper fieldMapper;
    private final ResultEntryTemplateResolver resultEntryTemplateResolver;

    @Override
    public String resolve(MessageTemplate template, TieredResultEntryTo value) {
        return StringSubstitutor.replace(template.getTemplate(), buildValuesMap(value)).trim();
    }

    public Map<String, String> buildValuesMap(TieredResultEntryTo entry) {
        Map<String, String> valueMap = resultEntryTemplateResolver.buildValuesMap(entry.entry());
        valueMap.put("tierName",String.valueOf(entry.tierName()));
        valueMap.put("validVehicle", entry.usesValidVehicle() ? "" : ":pineapple:");

        return valueMap;
    }
}
