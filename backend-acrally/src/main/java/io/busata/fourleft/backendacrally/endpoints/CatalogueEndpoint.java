package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.CarTo;
import io.busata.fourleft.api.acrally.models.VariantTo;
import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.car.CarService;
import io.busata.fourleft.backendacrally.domain.services.stage.LocationRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.StageRepository;
import io.busata.fourleft.backendacrally.domain.services.stage.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only catalogue for any signed-in user (not just admins): cars and variants, so a club owner
 * can pick them when scheduling a championship. The admin CRUD lives under {@code /admin/**}; these
 * are the plain member-facing reads.
 */
@RestController
@RequestMapping("/acrally-api")
@RequiredArgsConstructor
public class CatalogueEndpoint {

    private final CarService carService;
    private final VariantService variantService;
    private final StageRepository stageRepository;
    private final LocationRepository locationRepository;

    @GetMapping("/cars")
    public List<CarTo> cars() {
        return carService.list().stream()
                .map(car -> new CarTo(car.getId(), car.getName(), car.getYear(), car.getGroupName(),
                        car.getClassName(), car.getCreatedAt(), car.getUpdatedAt()))
                .toList();
    }

    @GetMapping("/variants")
    public List<VariantTo> variants() {
        Map<UUID, Stage> stages = stageRepository.findAll().stream()
                .collect(Collectors.toMap(Stage::getId, s -> s));
        Map<UUID, String> locationNames = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));

        return variantService.list().stream().map(variant -> {
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
