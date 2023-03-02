package io.busata.fourleft.endpoints.views;

import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.api.models.views.NoResultRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.api.models.views.SingleResultListTo;
import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import io.busata.fourleft.domain.options.models.Vehicle;
import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import io.busata.fourleft.domain.configuration.event_restrictions.repository.ViewEventRestrictionsRepository;
import io.busata.fourleft.endpoints.club.results.service.ResultEntryToFactory;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ViewResultToFactory {
    private final ClubSyncService clubSyncService;
    private final ClubViewRepository repository;
    private final ViewEventRestrictionsRepository viewEventRestrictionsRepository;
    private final ResultEntryToFactory resultEntryToFactory;
    private final BoardEntryFetcher boardEntryFetcher;

    @Transactional
    @Cacheable("view_results")
    public Optional<ViewResultTo> createViewResult(UUID viewId, ClubEventSupplierType type) {
        return repository.findById(viewId).map(clubView -> {
            switch(clubView.getResultsView()) {
                case SingleClubView view -> {
                    return createSingleClubViewResult(type.getSupplier(), view);
                }
                case TieredView view -> {
                    return createdTieredViewResult(type.getSupplier(), view);
                }
                default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
            }
        }).orElseThrow();
    }

    private Optional<ViewResultTo> createSingleClubViewResult(ClubEventSupplier eventSupplier, SingleClubView view) {
        final var club = clubSyncService.getOrCreate(view.getClubId());

        final var result = eventSupplier.getEvent(club)
                .map(event -> createSingleResultList(view, event))
                .stream()
                .toList();

        ViewResultTo viewResult = new ViewResultTo(
                view.getName(),
                new ViewPropertiesTo(view.isUsePowerStage(), view.getBadgeType()),
                result
        );

        return Optional.of(viewResult);
    }

    private SingleResultListTo createSingleResultList(SingleClubView view, Event event) {
        final var restrictions = viewEventRestrictionsRepository.findByResultViewIdAndChallengeIdAndEventId(view.getId(), event.getChallengeId(), event.getReferenceId());

        final ResultRestrictionsTo restrictionTo = restrictions.map(this::create).orElse(new NoResultRestrictionsTo());

        return this.createSingleResultTo(view, event, restrictionTo);
    }

    private Optional<ViewResultTo> createdTieredViewResult(ClubEventSupplier eventSupplier,  TieredView tieredView) {

        boolean anyEventActive = tieredView.getResultViews().stream()
                .map(SingleClubView::getClubId)
                .map(clubSyncService::getOrCreate)
                .map(eventSupplier::getEvent)
                .anyMatch(Optional::isPresent);

        if(!anyEventActive) {
            return Optional.empty();
        }

        final List<SingleResultListTo> results = tieredView.getResultViews().stream().map(resultViews -> {
            final var club = clubSyncService.getOrCreate(resultViews.getClubId());

            return eventSupplier.getEvent(club).map(event -> createSingleResultList(resultViews, event)).orElseThrow();

        }).collect(Collectors.toList());


        ViewResultTo result = new ViewResultTo(
                tieredView.getName(),
                new ViewPropertiesTo(tieredView.isUsePowerStage(), tieredView.getBadgeType()),
                results
        );

        return Optional.of(result);
    }



    public SingleResultListTo createSingleResultTo(SingleClubView view, Event event, ResultRestrictionsTo restrictions) {
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
                restrictions,
                totalEntries,
                results
        );
    }

    public ResultRestrictionsTo create(ViewEventRestrictions viewEventRestrictions) {
        return new ResultListRestrictionsTo(viewEventRestrictions.getVehicles().stream().map(
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
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.getName().toLowerCase())).collect(Collectors.toList());
    }
    private List<ResultEntryTo> filterResults(List<ResultEntryTo> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.name().toLowerCase())).collect(Collectors.toList());
    }

}
