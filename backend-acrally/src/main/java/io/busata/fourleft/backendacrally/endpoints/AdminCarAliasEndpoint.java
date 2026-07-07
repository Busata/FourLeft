package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.CarAliasCollectResultTo;
import io.busata.fourleft.api.acrally.models.CarAliasTo;
import io.busata.fourleft.api.acrally.models.UpdateCarAliasRequestTo;
import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.models.car.CarAlias;
import io.busata.fourleft.backendacrally.domain.services.car.CarAliasService;
import io.busata.fourleft.backendacrally.domain.services.car.CarRepository;
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
 * Admin surface for car aliases: list, collect (from results), assign to a catalogue car, delete.
 * Mirrors {@link AdminVariantEndpoint}. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule.
 */
@RestController
@RequestMapping("/acrally-api/admin/car-aliases")
@RequiredArgsConstructor
public class AdminCarAliasEndpoint {

    private final CarAliasService aliasService;
    private final CarRepository carRepository;

    @GetMapping("")
    public List<CarAliasTo> list() {
        return toTos(aliasService.list());
    }

    /** Scans results for distinct raw car strings and catalogues any new ones (existing rows untouched). */
    @PostMapping("/collect")
    public CarAliasCollectResultTo collect() {
        int added = aliasService.collect();
        return new CarAliasCollectResultTo(added, toTos(aliasService.list()));
    }

    @PutMapping("/{id}")
    public CarAliasTo update(@PathVariable UUID id, @RequestBody UpdateCarAliasRequestTo request) {
        return toTos(List.of(aliasService.assign(id, request.carId()))).get(0);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        aliasService.delete(id);
    }

    private List<CarAliasTo> toTos(List<CarAlias> aliases) {
        Map<UUID, String> carNames = carRepository.findAll().stream()
                .collect(Collectors.toMap(Car::getId, Car::getName));
        return aliases.stream()
                .map(alias -> new CarAliasTo(
                        alias.getId(),
                        alias.getRawName(),
                        alias.getCarId(),
                        alias.getCarId() == null ? null : carNames.get(alias.getCarId()),
                        alias.getCreatedAt(),
                        alias.getUpdatedAt()))
                .toList();
    }
}
