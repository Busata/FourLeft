package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.StageName;
import io.busata.fourleft.backendacrally.domain.services.session.StageResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StageNameService {

    private final StageNameRepository stageNameRepository;
    private final StageResultRepository stageResultRepository;

    public List<StageName> list() {
        return stageNameRepository.findAllByOrderByRawNameAsc();
    }

    /**
     * Scans the recorded results for distinct raw stage identifiers and inserts a catalogue row
     * for any not already present. Existing rows (and their assigned display names) are left
     * untouched. Returns the number of new rows created.
     */
    @Transactional
    public int collect() {
        Set<String> known = stageNameRepository.findAll().stream()
                .map(StageName::getRawName)
                .collect(Collectors.toSet());

        List<StageName> toAdd = stageResultRepository.findDistinctStageNames().stream()
                .filter(raw -> !known.contains(raw))
                .map(StageName::new)
                .toList();

        stageNameRepository.saveAll(toAdd);
        return toAdd.size();
    }

    @Transactional
    public StageName rename(java.util.UUID id, String displayName) {
        StageName stageName = stageNameRepository.findById(id).orElseThrow();
        stageName.rename(displayName);
        return stageName;
    }

    /**
     * A lookup from raw stage identifier to the readable name to render, for the raw names that
     * have an assigned display name. Callers fall back to the raw name for anything not present.
     */
    public Map<String, String> displayNameLookup() {
        return stageNameRepository.findAll().stream()
                .filter(stageName -> stageName.getDisplayName() != null)
                .collect(Collectors.toMap(StageName::getRawName, StageName::getDisplayName));
    }
}
