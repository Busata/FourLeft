package io.busata.fourleft.backendacrally.domain.services.car;

import io.busata.fourleft.backendacrally.domain.models.car.CarAlias;
import io.busata.fourleft.backendacrally.domain.services.session.StageResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Manages car aliases: collect raw car strings from results, assign them to catalogue cars, delete. */
@Service
@RequiredArgsConstructor
public class CarAliasService {

    private final CarAliasRepository aliasRepository;
    private final CarRepository carRepository;
    private final StageResultRepository stageResultRepository;

    public List<CarAlias> list() {
        return aliasRepository.findAllByOrderByRawNameAsc();
    }

    /**
     * Scans recorded results for distinct raw car strings and catalogues any not already present.
     * Existing aliases (and their assignments) are left untouched. Returns the number added.
     */
    @Transactional
    public int collect() {
        Set<String> known = aliasRepository.findAll().stream()
                .map(CarAlias::getRawName)
                .collect(Collectors.toSet());
        List<CarAlias> toAdd = stageResultRepository.findDistinctCarNames().stream()
                .filter(raw -> !known.contains(raw))
                .map(CarAlias::new)
                .toList();
        aliasRepository.saveAll(toAdd);
        return toAdd.size();
    }

    /** Point an alias at a catalogue car ({@code carId} null clears it). */
    @Transactional
    public CarAlias assign(UUID id, UUID carId) {
        CarAlias alias = aliasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such car alias."));
        if (carId != null && !carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected car does not exist.");
        }
        alias.assign(carId);
        return alias;
    }

    @Transactional
    public void delete(UUID id) {
        CarAlias alias = aliasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such car alias."));
        aliasRepository.delete(alias);
    }
}
