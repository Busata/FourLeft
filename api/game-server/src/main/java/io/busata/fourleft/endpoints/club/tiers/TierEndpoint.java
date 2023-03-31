package io.busata.fourleft.endpoints.club.tiers;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.PlayerCreateTo;
import io.busata.fourleft.api.models.tiers.PlayerTo;
import io.busata.fourleft.api.models.tiers.TierActiveInfoTo;
import io.busata.fourleft.api.models.tiers.TierCreateTo;
import io.busata.fourleft.api.models.tiers.TierTo;
import io.busata.fourleft.api.models.tiers.VehicleTo;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.configuration.results_views.TiersViewRepository;
import io.busata.fourleft.domain.tiers.models.Player;
import io.busata.fourleft.domain.tiers.models.Tier;
import io.busata.fourleft.domain.tiers.models.TierEventRestrictions;
import io.busata.fourleft.domain.tiers.repository.PlayerRepository;
import io.busata.fourleft.domain.tiers.repository.TierRepository;
import io.busata.fourleft.domain.tiers.repository.TierEventRestrictionsRepository;
import io.busata.fourleft.endpoints.club.tiers.models.CompetitionRestrictionCreateTo;
import io.busata.fourleft.endpoints.club.tiers.service.PlayerFactory;
import io.busata.fourleft.endpoints.club.tiers.service.TierFactory;
import io.busata.fourleft.importer.ClubSyncService;
import io.busata.fourleft.common.TransactionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController()
public class TierEndpoint {

    private final ClubSyncService clubSyncService;
    private final TierFactory tierFactory;
    private final PlayerFactory playerFactory;
    private final TierRepository tierRepository;
    private final PlayerRepository playerRepository;
    private final TierEventRestrictionsRepository tierEventRestrictionsRepository;

    private final TransactionHandler transactionHandler;

    private final TiersViewRepository tiersViewRepository;

    @GetMapping(Routes.CLUB_ACTIVE_EVENT_BY_CLUB_ID)
    public TierActiveInfoTo getActiveEventInfo(@PathVariable long clubId) {
        Club club = clubSyncService.getOrCreate(clubId);

        return club.getCurrentEvent().map(tierFactory::create).orElseThrow();
    }

    @PostMapping(Routes.CLUB_TIERS_BY_CLUB_ID)
    public TierTo createTier(@PathVariable long clubId, @RequestBody TierCreateTo tierCreateTo) {
        Tier entity = new Tier(clubId, tierCreateTo.name());
        final var tiersView = tiersViewRepository.findAll().get(0);
        entity.setTiersView(tiersView);
        return tierFactory.create(tierRepository.save(entity));
    }

    @GetMapping(Routes.CLUB_TIERS_BY_CLUB_ID)
    public List<TierTo> getTiers(@PathVariable long clubId) {
        return tierRepository.findAll().stream().map(tierFactory::create).toList();
    }

    @PostMapping(Routes.PLAYERS)
    public PlayerTo createPlayers(@RequestBody PlayerCreateTo playerCreateTo) {
        return playerFactory.create(playerRepository.save(
                new Player(playerCreateTo.racenet())
        ));
    }

    @DeleteMapping(Routes.PLAYER_BY_PLAYER_ID)
    public void createPlayer(@PathVariable UUID playerId) {
        playerRepository.deleteById(playerId);
    }

    @GetMapping(Routes.PLAYERS)
    public List<PlayerTo> getPlayers() {
        return playerRepository.findAll().stream()
                .filter(player -> player.getTiers().isEmpty())
                .map(playerFactory::create).toList();
    }

    @GetMapping(Routes.PLAYER_BY_TIER_ID)
    public List<PlayerTo> getPlayersByTier(@PathVariable UUID tierId) {
        return playerRepository.findByTiers_Id(tierId).stream().map(playerFactory::create).toList();
    }

    @PostMapping(Routes.TIER_PLAYER_BY_TIER_ID_AND_PLAYER_ID)
    public void assignPlayer(@PathVariable UUID tierId, @PathVariable UUID playerId) {
        transactionHandler.runInTransaction(() -> {
            Player player = playerRepository.getById(playerId);
            player.setTier(tierRepository.getById(tierId));
            playerRepository.save(player);
        });
    }

    @DeleteMapping(Routes.CLEAR_PLAYER_TIERS_BY_PLAYER_ID)
    public void clearTier(@PathVariable UUID playerId) {
        transactionHandler.runInTransaction(() -> {
            Player player = playerRepository.getById(playerId);
            player.clearTiers();
            playerRepository.save(player);
        });
    }

    @PostMapping(Routes.TIER_VEHICLE_RESTRICTIONS_BY_TIER_ID_AND_CHALLENGE_ID_AND_EVENT_ID)
    public void createCompetitionRestrictions(@PathVariable UUID tierId, @PathVariable String challengeId, @PathVariable String eventId,
                                              @RequestBody CompetitionRestrictionCreateTo request) {

        tierEventRestrictionsRepository.findByTierIdAndChallengeIdAndEventId(tierId, challengeId, eventId).ifPresentOrElse(tierEventRestrictions -> {
            tierEventRestrictions.updateVehicles(request.vehicles());
            tierEventRestrictionsRepository.save(tierEventRestrictions);
        }, () -> {
            final var tier = tierRepository.getById(tierId);

            final var tieredEvent = new TierEventRestrictions(tier, challengeId, eventId);
            tieredEvent.updateVehicles(request.vehicles());
            tierEventRestrictionsRepository.save(tieredEvent);
        });

    }
    @GetMapping(Routes.TIER_VEHICLE_RESTRICTIONS_BY_TIER_ID_AND_CHALLENGE_ID_AND_EVENT_ID)
    public List<VehicleTo> getCompetitionVehicles(@PathVariable UUID tierId, @PathVariable String challengeId, @PathVariable String eventId) {
        return this.tierEventRestrictionsRepository.findByTierIdAndChallengeIdAndEventId(tierId, challengeId, eventId)
                .map(tierEventRestrictions -> tierEventRestrictions.getVehicles().stream().map(tierFactory::create).collect(Collectors.toList()))
                .orElse(List.of());
    }
}
