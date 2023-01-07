package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.tiers.VehicleTo;
import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.api.models.views.NoResultRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.api.models.views.SingleResultListTo;
import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TiersView;
import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.domain.tiers.models.Tier;
import io.busata.fourleft.domain.tiers.models.TierEventRestrictions;
import io.busata.fourleft.domain.tiers.repository.TierEventRestrictionsRepository;
import io.busata.fourleft.endpoints.club.results.service.ResultEntryToFactory;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.List.of;

@Component
@RequiredArgsConstructor
public class ViewResultToFactory {
    private final ClubSyncService clubSyncService;
    private final ClubViewRepository repository;
    private final TierEventRestrictionsRepository tierEventRestrictionsRepository;
    private final ResultEntryToFactory resultEntryToFactory;
    private final BoardEntryFetcher boardEntryFetcher;

    public Optional<ViewResultTo> createViewResult(UUID viewId, ClubEventSupplier eventSupplier) {
        final var clubView = repository.findById(viewId).orElseThrow();

        switch(clubView.getResultsView()) {
            case SingleClubView view -> {
                return createSingleClubViewResult(eventSupplier, view);
            }
            case TiersView view -> {
                return createTiersViewResult(eventSupplier, view);
            }
            default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
        }
    }

    private Optional<ViewResultTo> createSingleClubViewResult(ClubEventSupplier eventSupplier, SingleClubView view) {
        final var club = clubSyncService.getOrCreate(view.getClubId());

        if(eventSupplier.getEvent(club).isEmpty()) {
            return Optional.empty();
        }

        SingleResultListTo singleResultListTo = eventSupplier.getEvent(club).map(event -> this.createSingleResultTo(view, event)).orElseThrow();

        ViewResultTo result = new ViewResultTo(
                new ViewPropertiesTo(view.isUsePowerStage(), view.getBadgeType()),
                of(singleResultListTo)
        );

        return Optional.of(result);
    }
    private Optional<ViewResultTo> createTiersViewResult(ClubEventSupplier eventSupplier, TiersView view) {

        boolean anyEventActive = view.getTiers().stream()
                .map(Tier::getClubId)
                .map(clubSyncService::getOrCreate)
                .map(eventSupplier::getEvent)
                .anyMatch(Optional::isPresent);

        if(!anyEventActive) {
            return Optional.empty();
        }

        final List<SingleResultListTo> allTierResults = view.getTiers().stream().map(tier -> {
            final var club = clubSyncService.getOrCreate(tier.getClubId());

            return eventSupplier.getEvent(club).map(event -> {
                final var restrictions = tierEventRestrictionsRepository.findByTierIdAndChallengeIdAndEventId(tier.getId(), event.getChallengeId(), event.getReferenceId());

                final ResultRestrictionsTo restrictionTo = restrictions.map(this::create).orElse(new NoResultRestrictionsTo());

                return this.createSingleResultTo(tier, view, event, restrictionTo);
            }).orElseThrow();

        }).collect(Collectors.toList());


        ViewResultTo result = new ViewResultTo(
                new ViewPropertiesTo(view.isUsePowerStage(), view.getBadgeType()),
                allTierResults
        );

        return Optional.of(result);
    }


    public SingleResultListTo createSingleResultTo(SingleClubView view, Event event) {
        List<BoardEntry> entries = boardEntryFetcher.create(view, event);

        if(view.getPlayerRestrictions() == PlayerRestrictions.EXCLUDE) {
            entries = filterEntries(entries, view.getPlayerNames());
        }
        int totalEntries = entries.size();

        List<ResultEntryTo> results = resultEntryToFactory.create(entries);

        if(view.getPlayerRestrictions() == PlayerRestrictions.FILTER) {
            results = filterResults(results, view.getPlayerNames());
        }

        return new SingleResultListTo(
                event.getChampionship().getClub().getName(),
                createEventInfo(event),
                new NoResultRestrictionsTo(),
                totalEntries,
                results
        );
    }

    public SingleResultListTo createSingleResultTo(Tier tier, TiersView view, Event event, ResultRestrictionsTo eventRestrictions){
        List<BoardEntry> entries = boardEntryFetcher.create(view, event);
        int totalEntries = entries.size();

        entries = filterEntries(entries, tier.getPlayerNames());

        return new SingleResultListTo(
                tier.getName(),
                createEventInfo(event),
                eventRestrictions,
                totalEntries,
                resultEntryToFactory.create(entries)
        );
    }

    public ResultRestrictionsTo create(TierEventRestrictions tierEventRestrictions) {
        return new ResultListRestrictionsTo(tierEventRestrictions.getVehicles().stream().map(
                this::createVehicle).collect(Collectors.toList()));

    }
    public VehicleTo createVehicle(Vehicle vehicle) {
        return new VehicleTo(
                vehicle.name(),
                vehicle.getDisplayName()
        );
    }
    public EventInfoTo createEventInfo(Event event) {
        return new EventInfoTo(event.getReferenceId(),
                event.getChallengeId(),
                event.getName(),
                event.getStages().stream().map(Stage::getName).collect(Collectors.toList()),
                event.getVehicleClass(),
                event.getCountry(),
                event.getLastResultCheckedTime(),
                event.getEndTime());
    }

    private List<BoardEntry> filterEntries(List<BoardEntry> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).collect(Collectors.toList());

        return entries.stream().filter(entry -> sanitized.contains(entry.getName().toLowerCase())).collect(Collectors.toList());
    }
    private List<ResultEntryTo> filterResults(List<ResultEntryTo> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).collect(Collectors.toList());

        return entries.stream().filter(entry -> sanitized.contains(entry.name().toLowerCase())).collect(Collectors.toList());
    }

}
