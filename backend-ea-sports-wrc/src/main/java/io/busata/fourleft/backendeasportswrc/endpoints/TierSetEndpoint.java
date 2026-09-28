package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.TierLabelTo;
import io.busata.fourleft.api.easportswrc.models.TierOrderTo;
import io.busata.fourleft.api.easportswrc.models.TierPlayerAssignTo;
import io.busata.fourleft.api.easportswrc.models.TierPlayerTo;
import io.busata.fourleft.api.easportswrc.models.TierSetCreateTo;
import io.busata.fourleft.api.easportswrc.models.TierSetLinkRequestTo;
import io.busata.fourleft.api.easportswrc.models.TierSetLinkTo;
import io.busata.fourleft.api.easportswrc.models.TierSetTo;
import io.busata.fourleft.api.easportswrc.models.TierSetUpdateTo;
import io.busata.fourleft.backendeasportswrc.application.tiers.TierSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Tier sets. The first three calls come from the Discord bot (/fourleft tiers ...); everything under
 * /link/{linkId} is the edit page, where the link id is the credential.
 */
@RestController
@RequiredArgsConstructor
public class TierSetEndpoint {

    private final TierSetService tierSetService;

    @PostMapping("/api_v2/tier-sets")
    public TierSetLinkTo create(@RequestBody TierSetCreateTo request) {
        return tierSetService.create(request.name(), request.guildId(), request.discordId());
    }

    @GetMapping("/api_v2/tier-sets/guild/{guildId}/names")
    public List<String> namesForGuild(@PathVariable Long guildId) {
        return tierSetService.namesForGuild(guildId);
    }

    @PostMapping("/api_v2/tier-sets/link-request")
    public ResponseEntity<TierSetLinkTo> requestLink(@RequestBody TierSetLinkRequestTo request) {
        return ResponseEntity.of(tierSetService.requestLink(request.guildId(), request.name(), request.discordId()));
    }

    @GetMapping("/api_v2/tier-sets/link/{linkId}")
    public ResponseEntity<TierSetTo> get(@PathVariable UUID linkId) {
        return ResponseEntity.of(tierSetService.get(linkId));
    }

    @PutMapping("/api_v2/tier-sets/link/{linkId}")
    public ResponseEntity<TierSetTo> rename(@PathVariable UUID linkId, @RequestBody TierSetUpdateTo request) {
        return ResponseEntity.of(tierSetService.rename(linkId, request.name()));
    }

    @PostMapping("/api_v2/tier-sets/link/{linkId}/tiers")
    public ResponseEntity<TierSetTo> addTier(@PathVariable UUID linkId, @RequestBody TierLabelTo request) {
        return ResponseEntity.of(tierSetService.addTier(linkId, request.label()));
    }

    @PutMapping("/api_v2/tier-sets/link/{linkId}/tiers/{tierId}")
    public ResponseEntity<TierSetTo> relabelTier(@PathVariable UUID linkId, @PathVariable UUID tierId, @RequestBody TierLabelTo request) {
        return ResponseEntity.of(tierSetService.relabelTier(linkId, tierId, request.label()));
    }

    @DeleteMapping("/api_v2/tier-sets/link/{linkId}/tiers/{tierId}")
    public ResponseEntity<TierSetTo> removeTier(@PathVariable UUID linkId, @PathVariable UUID tierId) {
        return ResponseEntity.of(tierSetService.removeTier(linkId, tierId));
    }

    @PutMapping("/api_v2/tier-sets/link/{linkId}/tiers/order")
    public ResponseEntity<TierSetTo> reorderTiers(@PathVariable UUID linkId, @RequestBody TierOrderTo request) {
        return ResponseEntity.of(tierSetService.reorderTiers(linkId, request.tierIds()));
    }

    @PutMapping("/api_v2/tier-sets/link/{linkId}/players")
    public ResponseEntity<TierSetTo> assignPlayer(@PathVariable UUID linkId, @RequestBody TierPlayerAssignTo request) {
        return ResponseEntity.of(tierSetService.assignPlayer(linkId, request.tierId(), request.playerId(), request.displayName()));
    }

    @DeleteMapping("/api_v2/tier-sets/link/{linkId}/players/{playerId}")
    public ResponseEntity<TierSetTo> unassignPlayer(@PathVariable UUID linkId, @PathVariable String playerId) {
        return ResponseEntity.of(tierSetService.unassignPlayer(linkId, playerId));
    }

    @GetMapping("/api_v2/tier-sets/link/{linkId}/players/suggest")
    public ResponseEntity<List<TierPlayerTo>> suggestPlayers(@PathVariable UUID linkId, @RequestParam String q) {
        return ResponseEntity.of(tierSetService.suggestPlayers(linkId, q));
    }
}
