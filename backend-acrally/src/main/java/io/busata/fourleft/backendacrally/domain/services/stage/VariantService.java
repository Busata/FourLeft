package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.Location;
import io.busata.fourleft.backendacrally.domain.models.stage.Stage;
import io.busata.fourleft.backendacrally.domain.models.stage.Variant;
import io.busata.fourleft.backendacrally.domain.services.session.StageResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantService {

    private final VariantRepository variantRepository;
    private final StageRepository stageRepository;
    private final LocationRepository locationRepository;
    private final StageResultRepository stageResultRepository;

    public List<Variant> list() {
        return variantRepository.findAllByOrderByRawNameAsc();
    }

    /**
     * Scans the recorded results for distinct raw stage identifiers and inserts a variant for any
     * not already present. Existing variants (their display names and stage assignments) are left
     * untouched. Returns the number of new variants created.
     */
    @Transactional
    public int collect() {
        Set<String> known = variantRepository.findAll().stream()
                .map(Variant::getRawName)
                .collect(Collectors.toSet());

        List<Variant> toAdd = stageResultRepository.findDistinctStageNames().stream()
                .filter(raw -> !known.contains(raw))
                .map(Variant::new)
                .toList();

        variantRepository.saveAll(toAdd);
        return toAdd.size();
    }

    @Transactional
    public Variant update(UUID id, String displayName, UUID stageId) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (stageId != null && !stageRepository.existsById(stageId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected stage does not exist.");
        }
        variant.update(displayName, stageId);
        return variant;
    }

    @Transactional
    public void delete(UUID id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        variantRepository.delete(variant);
    }

    /** A variant's resolved labels for display: its own label plus its stage and location names. */
    public record VariantLabel(String label, String stageName, String locationName) {
    }

    /**
     * Resolved display labels keyed by variant id (variant → stage → location), for callers that
     * render variants selected into events (the schedule, leaderboards, the agent's race list).
     */
    public Map<UUID, VariantLabel> labelsById() {
        Map<UUID, Stage> stages = stageRepository.findAll().stream()
                .collect(Collectors.toMap(Stage::getId, s -> s));
        Map<UUID, String> locationNames = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));
        return variantRepository.findAll().stream()
                .collect(Collectors.toMap(Variant::getId, variant -> toLabel(variant, stages, locationNames)));
    }

    private VariantLabel toLabel(Variant variant, Map<UUID, Stage> stages, Map<UUID, String> locationNames) {
        String label = variant.getDisplayName() != null ? variant.getDisplayName() : variant.getRawName();
        Stage stage = variant.getStageId() == null ? null : stages.get(variant.getStageId());
        String stageName = stage == null ? null : stage.getName();
        String locationName = stage == null || stage.getLocationId() == null ? null
                : locationNames.get(stage.getLocationId());
        return new VariantLabel(label, stageName, locationName);
    }

    /**
     * A lookup from raw stage identifier to the readable name to render, resolving each variant up
     * the hierarchy (variant → stage → location). Callers fall back to the raw name for anything
     * not present here.
     */
    public Map<String, String> displayNameLookup() {
        Map<UUID, Stage> stages = stageRepository.findAll().stream()
                .collect(Collectors.toMap(Stage::getId, s -> s));
        Map<UUID, String> locationNames = locationRepository.findAll().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));

        return variantRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Variant::getRawName,
                        variant -> resolveLabel(variant, stages, locationNames),
                        (a, b) -> a));
    }

    /** Builds "Location — Stage (variant)" from whatever parts are assigned, falling back to the raw name. */
    private String resolveLabel(Variant variant, Map<UUID, Stage> stages, Map<UUID, String> locationNames) {
        Stage stage = variant.getStageId() == null ? null : stages.get(variant.getStageId());
        if (stage == null) {
            return variant.getDisplayName() != null ? variant.getDisplayName() : variant.getRawName();
        }
        String locationName = stage.getLocationId() == null ? null : locationNames.get(stage.getLocationId());
        String base = locationName != null ? locationName + " — " + stage.getName() : stage.getName();
        return variant.getDisplayName() != null ? base + " (" + variant.getDisplayName() + ")" : base;
    }
}
