package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.UpdateVariantRequestTo;
import io.busata.fourleft.api.acrally.models.VariantCollectResultTo;
import io.busata.fourleft.api.acrally.models.VariantTo;
import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.stage.LocationRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.StageRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantService;
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

/**
 * Admin surface for the variant catalogue: list, collect (from results), assign a display name and
 * stage, and delete. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule.
 */
@RestController
@RequestMapping("/acrally-api/admin/variants")
@RequiredArgsConstructor
public class AdminVariantEndpoint {

    private final VariantService variantService;
    private final StageRepository stageRepository;
    private final LocationRepository locationRepository;

    @GetMapping("")
    public List<VariantTo> list() {
        return toTos(variantService.list());
    }

    /** Scans results for distinct raw stage keys and catalogues any new ones (existing rows untouched). */
    @PostMapping("/collect")
    public VariantCollectResultTo collect() {
        int added = variantService.collect();
        return new VariantCollectResultTo(added, toTos(variantService.list()));
    }

    @PutMapping("/{id}")
    public VariantTo update(@PathVariable UUID id, @RequestBody UpdateVariantRequestTo request) {
        return toTos(List.of(variantService.update(id, request.displayName(), request.stageId()))).get(0);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        variantService.delete(id);
    }

    private List<VariantTo> toTos(List<Variant> variants) {
        Map<UUID, Stage> stages = stageRepository.findAll().stream()
                .collect(Collectors.toMap(Stage::getId, s -> s));
        Map<UUID, String> locationNames = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));

        return variants.stream().map(variant -> {
            Stage stage = variant.getStageId() == null ? null : stages.get(variant.getStageId());
            String stageName = stage == null ? null : stage.getName();
            String locationName = (stage == null || stage.getLocationId() == null)
                    ? null : locationNames.get(stage.getLocationId());
            return new VariantTo(
                    variant.getId(),
                    variant.getRawName(),
                    variant.getDisplayName(),
                    variant.getStageId(),
                    stageName,
                    locationName,
                    variant.getCreatedAt(),
                    variant.getUpdatedAt());
        }).toList();
    }
}
