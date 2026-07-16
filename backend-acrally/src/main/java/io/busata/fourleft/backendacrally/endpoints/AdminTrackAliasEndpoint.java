package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.CreateTrackAliasRequestTo;
import io.busata.fourleft.api.acrally.models.TrackAliasCollectResultTo;
import io.busata.fourleft.api.acrally.models.TrackAliasTo;
import io.busata.fourleft.api.acrally.models.UpdateTrackAliasRequestTo;
import io.busata.fourleft.backendacrally.domain.models.stage.TrackAlias;
import io.busata.fourleft.backendacrally.domain.services.stage.TrackAliasService;
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

/**
 * Admin surface for track aliases: list, collect (from sessions), assign to a variant, delete.
 * Mirrors {@link AdminCarAliasEndpoint}. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule.
 */
@RestController
@RequestMapping("/acrally-api/admin/track-aliases")
@RequiredArgsConstructor
public class AdminTrackAliasEndpoint {

    private final TrackAliasService aliasService;
    private final VariantService variantService;

    @GetMapping("")
    public List<TrackAliasTo> list() {
        return toTos(aliasService.list());
    }

    /** Add an alias by hand (the exact string telemetry reports), optionally assigned to a variant. */
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackAliasTo create(@RequestBody CreateTrackAliasRequestTo request) {
        return toTos(List.of(aliasService.create(request.rawName(), request.variantId()))).get(0);
    }

    /** Scans sessions for distinct telemetry track strings and catalogues any new ones (existing rows untouched). */
    @PostMapping("/collect")
    public TrackAliasCollectResultTo collect() {
        int added = aliasService.collect();
        return new TrackAliasCollectResultTo(added, toTos(aliasService.list()));
    }

    @PutMapping("/{id}")
    public TrackAliasTo update(@PathVariable UUID id, @RequestBody UpdateTrackAliasRequestTo request) {
        return toTos(List.of(aliasService.assign(id, request.variantId()))).get(0);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        aliasService.delete(id);
    }

    private List<TrackAliasTo> toTos(List<TrackAlias> aliases) {
        Map<UUID, VariantService.VariantLabel> labels = variantService.labelsById();
        return aliases.stream()
                .map(alias -> new TrackAliasTo(
                        alias.getId(),
                        alias.getRawName(),
                        alias.getVariantId(),
                        alias.getVariantId() == null ? null
                                : labels.containsKey(alias.getVariantId())
                                        ? labels.get(alias.getVariantId()).fullLabel() : null,
                        alias.getCreatedAt(),
                        alias.getUpdatedAt()))
                .toList();
    }
}
