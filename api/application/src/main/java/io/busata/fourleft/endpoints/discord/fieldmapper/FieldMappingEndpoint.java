package io.busata.fourleft.endpoints.discord.fieldmapper;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.FieldMappingRequestTo;
import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.api.models.FieldMappingUpdateTo;
import io.busata.fourleft.domain.discord.models.FieldMapping;
import io.busata.fourleft.domain.discord.repository.FieldMappingRepository;
import io.busata.fourleft.endpoints.discord.fieldmapper.service.FieldMappingToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class FieldMappingEndpoint {

    private final FieldMappingRepository fieldMappingRepository;
    private final FieldMappingToFactory factory;

        @GetMapping(Routes.FIELD_MAPPINGS)
    List<FieldMappingTo> getFieldMappings() {
        return fieldMappingRepository.findAll().stream().map(factory::create).collect(Collectors.toList());
    }

    @PostMapping(Routes.FIELD_MAPPINGS)
    FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request) {
        return factory.create(this.fieldMappingRepository.save(new FieldMapping(request.name(), request.type())));
    }

    @PutMapping(Routes.FIELD_MAPPING_BY_ID)
    @Transactional
    FieldMappingTo updateFieldMapping(@RequestBody FieldMappingUpdateTo request, @PathVariable UUID id) {
        FieldMapping fieldMapping = this.fieldMappingRepository.findById(id).orElseThrow();
        fieldMapping.setName(request.name());
        fieldMapping.setValue(request.value());
        fieldMapping.setType(request.fieldMappingType());
        fieldMapping.setMappedByUser(true);

        return this.factory.create(this.fieldMappingRepository.save(fieldMapping));
    }
}
