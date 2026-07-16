package io.busata.fourleft.backendacrally.domain.services.stage;

import io.busata.fourleft.backendacrally.domain.models.stage.TrackAlias;
import io.busata.fourleft.backendacrally.domain.services.session.AgentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Manages track aliases: collect telemetry track strings from sessions, assign them to variants, delete. */
@Service
@RequiredArgsConstructor
public class TrackAliasService {

    private final TrackAliasRepository aliasRepository;
    private final VariantRepository variantRepository;
    private final AgentSessionRepository sessionRepository;

    private static final int MAX_RAW_LENGTH = 190;

    public List<TrackAlias> list() {
        return aliasRepository.findAllByOrderByRawNameAsc();
    }

    /**
     * Add an alias by hand: the exact track string telemetry reports, optionally already assigned to
     * a variant. Complements {@link #collect()} for cases where the admin knows the string upfront.
     */
    @Transactional
    public TrackAlias create(String rawName, UUID variantId) {
        String raw = rawName == null ? "" : rawName.strip();
        if (raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The track name telemetry reports is required.");
        }
        if (raw.length() > MAX_RAW_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "That track name is too long (max %d characters).".formatted(MAX_RAW_LENGTH));
        }
        if (aliasRepository.existsByRawName(raw)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That track name is already listed.");
        }
        if (variantId != null && !variantRepository.existsById(variantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected stage does not exist.");
        }
        TrackAlias alias = new TrackAlias(raw);
        alias.assign(variantId);
        return aliasRepository.save(alias);
    }

    /**
     * Scans sessions for distinct telemetry track strings and catalogues any not already present.
     * Existing aliases (and their assignments) are left untouched. Returns the number added.
     */
    @Transactional
    public int collect() {
        Set<String> known = aliasRepository.findAll().stream()
                .map(TrackAlias::getRawName)
                .collect(Collectors.toSet());
        List<TrackAlias> toAdd = sessionRepository.findDistinctTrackNames().stream()
                .filter(raw -> !known.contains(raw))
                .map(TrackAlias::new)
                .toList();
        aliasRepository.saveAll(toAdd);
        return toAdd.size();
    }

    /** Point an alias at a variant ({@code variantId} null clears it). */
    @Transactional
    public TrackAlias assign(UUID id, UUID variantId) {
        TrackAlias alias = aliasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such track alias."));
        if (variantId != null && !variantRepository.existsById(variantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected stage does not exist.");
        }
        alias.assign(variantId);
        return alias;
    }

    @Transactional
    public void delete(UUID id) {
        TrackAlias alias = aliasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such track alias."));
        aliasRepository.delete(alias);
    }
}
