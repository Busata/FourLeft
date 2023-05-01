package io.busata.fourleft.application.discord;

import io.busata.fourleft.api.models.FieldMappingRequestTo;
import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.api.models.FieldMappingUpdateTo;
import io.busata.fourleft.domain.discord.bot.models.FieldMapping;
import io.busata.fourleft.domain.discord.bot.repository.FieldMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FieldMappingService {

    private final FieldMappingRepository fieldMappingRepository;
    private final FieldMappingToFactory factory;

    public List<FieldMappingTo> getFieldMappings() {
        return fieldMappingRepository.findAll().stream().map(factory::create).collect(Collectors.toList());
    }

    public FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request) {
       return this.fieldMappingRepository.findByTypeAndNameAndContext(request.type(), request.name(), request.context()).map(factory::create).orElseGet(() -> {
           return factory.create(this.fieldMappingRepository.save(new FieldMapping(request.name(), request.type(), request.context())));
       });
    }

    @Transactional
    public FieldMappingTo updateFieldMapping(@RequestBody FieldMappingUpdateTo request, @PathVariable UUID id) {
        FieldMapping fieldMapping = this.fieldMappingRepository.findById(id).orElseThrow();
        fieldMapping.setName(request.name());
        fieldMapping.setValue(request.value());
        fieldMapping.setType(request.fieldMappingType());
        fieldMapping.setMappedByUser(true);

        return this.factory.create(this.fieldMappingRepository.save(fieldMapping));
    }
}
