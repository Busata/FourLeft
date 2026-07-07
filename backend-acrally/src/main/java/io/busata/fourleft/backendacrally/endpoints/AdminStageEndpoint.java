package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.StageRequestTo;
import io.busata.fourleft.api.acrally.models.StageTo;
import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import io.busata.fourleft.backendacrally.domain.services.stage.LocationRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.StageService;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Admin CRUD for stages. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule. */
@RestController
@RequestMapping("/acrally-api/admin/stages")
@RequiredArgsConstructor
public class AdminStageEndpoint {

    private final StageService stageService;
    private final LocationRepository locationRepository;
    private final VariantRepository variantRepository;

    @GetMapping("")
    public List<StageTo> list() {
        Map<UUID, String> locationNames = locationNames();
        return stageService.list().stream().map(stage -> toTo(stage, locationNames)).toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public StageTo create(@RequestBody StageRequestTo request) {
        return toTo(stageService.create(request.name(), request.locationId()), locationNames());
    }

    @PutMapping("/{id}")
    public StageTo update(@PathVariable UUID id, @RequestBody StageRequestTo request) {
        return toTo(stageService.update(id, request.name(), request.locationId()), locationNames());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        stageService.delete(id);
    }

    private Map<UUID, String> locationNames() {
        return locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));
    }

    private StageTo toTo(Stage stage, Map<UUID, String> locationNames) {
        return new StageTo(
                stage.getId(),
                stage.getName(),
                stage.getLocationId(),
                stage.getLocationId() == null ? null : locationNames.get(stage.getLocationId()),
                variantRepository.countByStageId(stage.getId()),
                stage.getCreatedAt(),
                stage.getUpdatedAt());
    }
}
