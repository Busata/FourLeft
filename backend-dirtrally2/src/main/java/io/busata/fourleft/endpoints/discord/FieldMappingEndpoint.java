package io.busata.fourleft.endpoints.discord;


import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.FieldMappingRequestTo;
import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.api.models.FieldMappingUpdateTo;
import io.busata.fourleft.application.discord.FieldMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FieldMappingEndpoint {

    private final FieldMappingService fieldMappingService;

    @GetMapping(RoutesTo.FIELD_MAPPINGS)
    public List<FieldMappingTo> getFieldMappings() {
        return fieldMappingService.getFieldMappings();
    }

    @PostMapping(RoutesTo.FIELD_MAPPINGS)
    public FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request) {
        return fieldMappingService.createFieldMapping(request);
    }

    @PutMapping(RoutesTo.FIELD_MAPPING_BY_ID)
    public FieldMappingTo updateFieldMapping(@RequestBody FieldMappingUpdateTo request, @PathVariable UUID id) {
        return fieldMappingService.updateFieldMapping(request, id);
    }
}
