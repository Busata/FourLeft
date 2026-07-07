package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final StageRepository stageRepository;

    public List<Location> list() {
        return locationRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Location create(String name, String nation) {
        String cleaned = require(name);
        if (locationRepository.existsByNameIgnoreCase(cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A location with that name already exists.");
        }
        return locationRepository.save(new Location(cleaned, nation));
    }

    @Transactional
    public Location update(UUID id, String name, String nation) {
        Location location = find(id);
        String cleaned = require(name);
        if (!location.getName().equalsIgnoreCase(cleaned) && locationRepository.existsByNameIgnoreCase(cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A location with that name already exists.");
        }
        location.update(cleaned, nation);
        return location;
    }

    @Transactional
    public void delete(UUID id) {
        Location location = find(id);
        if (stageRepository.existsByLocationId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This location still has stages assigned to it.");
        }
        locationRepository.delete(location);
    }

    private Location find(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String require(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A name is required.");
        }
        return name.strip();
    }
}
