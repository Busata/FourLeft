package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StageService {

    private final StageRepository stageRepository;
    private final LocationRepository locationRepository;
    private final VariantRepository variantRepository;

    public List<Stage> list() {
        return stageRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Stage create(String name, UUID locationId) {
        String cleaned = require(name);
        validateLocation(locationId);
        return stageRepository.save(new Stage(cleaned, locationId));
    }

    @Transactional
    public Stage update(UUID id, String name, UUID locationId) {
        Stage stage = find(id);
        String cleaned = require(name);
        validateLocation(locationId);
        stage.update(cleaned, locationId);
        return stage;
    }

    @Transactional
    public void delete(UUID id) {
        Stage stage = find(id);
        if (variantRepository.existsByStageId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This stage still has variants assigned to it.");
        }
        stageRepository.delete(stage);
    }

    private Stage find(UUID id) {
        return stageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validateLocation(UUID locationId) {
        if (locationId != null && !locationRepository.existsById(locationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected location does not exist.");
        }
    }

    private String require(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A name is required.");
        }
        return name.strip();
    }
}
