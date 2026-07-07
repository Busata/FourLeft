package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.LocationRequestTo;
import io.busata.fourleft.api.acrally.models.LocationTo;
import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import io.busata.fourleft.backendacrally.domain.services.stage.LocationService;
import io.busata.fourleft.backendacrally.domain.services.stage.StageRepository;
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
import java.util.UUID;

/** Admin CRUD for rally locations. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule. */
@RestController
@RequestMapping("/acrally-api/admin/locations")
@RequiredArgsConstructor
public class AdminLocationEndpoint {

    private final LocationService locationService;
    private final StageRepository stageRepository;

    @GetMapping("")
    public List<LocationTo> list() {
        return locationService.list().stream().map(this::toTo).toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationTo create(@RequestBody LocationRequestTo request) {
        return toTo(locationService.create(request.name(), request.nation()));
    }

    @PutMapping("/{id}")
    public LocationTo update(@PathVariable UUID id, @RequestBody LocationRequestTo request) {
        return toTo(locationService.update(id, request.name(), request.nation()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        locationService.delete(id);
    }

    private LocationTo toTo(Location location) {
        return new LocationTo(
                location.getId(),
                location.getName(),
                location.getNation(),
                stageRepository.countByLocationId(location.getId()),
                location.getCreatedAt(),
                location.getUpdatedAt());
    }
}
