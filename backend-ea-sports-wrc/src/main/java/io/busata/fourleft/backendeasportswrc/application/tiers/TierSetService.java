package io.busata.fourleft.backendeasportswrc.application.tiers;

import io.busata.fourleft.api.easportswrc.models.TierPlayerTo;
import io.busata.fourleft.api.easportswrc.models.TierSetLinkTo;
import io.busata.fourleft.api.easportswrc.models.TierSetTo;
import io.busata.fourleft.api.easportswrc.models.TierTo;
import io.busata.fourleft.backendeasportswrc.domain.models.tiers.Tier;
import io.busata.fourleft.backendeasportswrc.domain.models.tiers.TierPlayer;
import io.busata.fourleft.backendeasportswrc.domain.models.tiers.TierSet;
import io.busata.fourleft.backendeasportswrc.domain.models.tiers.TierSetLink;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Tier sets: created from Discord (/fourleft tiers create) or the CLI, then managed through a private edit
 * link. Every link-scoped call returns the whole set, so the page just re-renders what comes back; an
 * unknown link is empty (404 at the endpoint).
 */
@Service
@RequiredArgsConstructor
public class TierSetService {

    static final int MAX_NAME_LENGTH = 100;

    private final TierSetRepository tierSetRepository;
    private final TierSetLinkRepository linkRepository;
    private final PlayerSuggestions playerSuggestions;

    /** Creates a set and its first edit link. Names are unique per guild, case-insensitively. */
    @Transactional
    public TierSetLinkTo create(String name, Long guildId, String discordId) {
        String cleanName = requireName(name, "Name");
        requireUniqueName(guildId, cleanName, null);

        TierSet tierSet = tierSetRepository.save(new TierSet(cleanName, guildId, discordId));
        return newLink(tierSet, discordId);
    }

    @Transactional(readOnly = true)
    public List<String> namesForGuild(Long guildId) {
        return tierSetRepository.findByGuildIdOrderByNameAsc(guildId).stream().map(TierSet::getName).toList();
    }

    /** A new edit link for the guild's set with this name; empty when there is none. */
    @Transactional
    public Optional<TierSetLinkTo> requestLink(Long guildId, String name, String discordId) {
        if (guildId == null || name == null) {
            return Optional.empty();
        }
        return tierSetRepository.findByGuildIdAndNameIgnoreCase(guildId, name.trim())
                .map(tierSet -> newLink(tierSet, discordId));
    }

    @Transactional(readOnly = true)
    public Optional<TierSetTo> get(UUID linkId) {
        return findByLink(linkId).map(this::toTo);
    }

    @Transactional
    public Optional<TierSetTo> rename(UUID linkId, String name) {
        return edit(linkId, tierSet -> {
            String cleanName = requireName(name, "Name");
            requireUniqueName(tierSet.getGuildId(), cleanName, tierSet.getId());
            tierSet.rename(cleanName);
        });
    }

    @Transactional
    public Optional<TierSetTo> addTier(UUID linkId, String label) {
        return edit(linkId, tierSet -> tierSet.addTier(requireName(label, "Label")));
    }

    @Transactional
    public Optional<TierSetTo> relabelTier(UUID linkId, UUID tierId, String label) {
        return edit(linkId, tierSet -> requireTier(tierSet, tierId).setLabel(requireName(label, "Label")));
    }

    @Transactional
    public Optional<TierSetTo> removeTier(UUID linkId, UUID tierId) {
        return edit(linkId, tierSet -> {
            if (!tierSet.removeTier(tierId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tier");
            }
        });
    }

    @Transactional
    public Optional<TierSetTo> reorderTiers(UUID linkId, List<UUID> tierIds) {
        return edit(linkId, tierSet -> tierSet.reorder(tierIds == null ? List.of() : tierIds));
    }

    @Transactional
    public Optional<TierSetTo> assignPlayer(UUID linkId, UUID tierId, String playerId, String displayName) {
        return edit(linkId, tierSet -> {
            String cleanPlayerId = requireName(playerId, "Player");
            String cleanName = Optional.ofNullable(displayName).map(String::trim).filter(n -> !n.isEmpty()).orElse(cleanPlayerId);
            if (!tierSet.assignPlayer(tierId, cleanPlayerId, cleanName)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tier");
            }
        });
    }

    @Transactional
    public Optional<TierSetTo> unassignPlayer(UUID linkId, String playerId) {
        return edit(linkId, tierSet -> tierSet.unassignPlayer(playerId));
    }

    /** Player autocomplete for a set's edit page; empty for an unknown link. */
    @Transactional(readOnly = true)
    public Optional<List<TierPlayerTo>> suggestPlayers(UUID linkId, String query) {
        return findByLink(linkId).map(tierSet -> playerSuggestions.suggest(query));
    }

    private Optional<TierSetTo> edit(UUID linkId, Consumer<TierSet> change) {
        return findByLink(linkId).map(tierSet -> {
            change.accept(tierSet);
            // Assigns the ids of newly added tiers before they go back to the page.
            tierSetRepository.flush();
            return toTo(tierSet);
        });
    }

    private Optional<TierSet> findByLink(UUID linkId) {
        return linkRepository.findById(linkId).flatMap(link -> tierSetRepository.findById(link.getTierSetId()));
    }

    private TierSetLinkTo newLink(TierSet tierSet, String discordId) {
        TierSetLink link = linkRepository.save(new TierSetLink(tierSet.getId(), discordId));
        return new TierSetLinkTo(link.getId(), tierSet.getName());
    }

    private void requireUniqueName(Long guildId, String name, UUID ownId) {
        if (guildId == null) {
            return;
        }
        tierSetRepository.findByGuildIdAndNameIgnoreCase(guildId, name)
                .filter(existing -> !Objects.equals(existing.getId(), ownId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A tier set named '%s' already exists".formatted(existing.getName()));
                });
    }

    private static Tier requireTier(TierSet tierSet, UUID tierId) {
        return tierSet.findTier(tierId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tier"));
    }

    private static String requireName(String value, String field) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty() || clean.length() > MAX_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "%s must be 1-%d characters".formatted(field, MAX_NAME_LENGTH));
        }
        return clean;
    }

    private TierSetTo toTo(TierSet tierSet) {
        return new TierSetTo(tierSet.getId(), tierSet.getName(), tierSet.getTiers().stream()
                .map(tier -> new TierTo(tier.getId(), tier.getLabel(), tier.getPlayers().stream()
                        .sorted(Comparator.comparing(TierPlayer::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                        .map(player -> new TierPlayerTo(player.getPlayerId(), player.getDisplayName()))
                        .toList()))
                .toList());
    }
}
