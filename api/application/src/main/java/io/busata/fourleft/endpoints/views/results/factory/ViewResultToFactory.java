package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.results_views.ConcatenationView;
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.configuration.results_views.PartitionView;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.EventSupplier;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ViewResultToFactory {
    private final ClubSyncService clubSyncService;
    private final ClubViewRepository clubViewRepository;
    private final ResultListToFactory resultListToFactory;
    private final ResultListMerger resultListMerger;
    private final ResultListPartitioner resultListPartitioner;

    @Transactional
    @Cacheable("view_results")
    public Optional<ViewResultTo> createViewResult(UUID viewId, EventSupplier type) {
        return clubViewRepository.findById(viewId).flatMap(clubView -> {
            return createViewResultFromResultsView(clubView, clubView.getResultsView(), type);
        });
    }

    public Optional<ViewResultTo> createViewResultFromResultsView(ClubView clubView, ResultsView resultsView, EventSupplier type) {
        return switch (resultsView) {
            case SingleClubView typedResultsView -> createSingleClubViewResult(clubView, typedResultsView, type);
            case MergeResultsView typedResultsView -> createMergedView(clubView, typedResultsView, type);
            case ConcatenationView typedResultsView -> createConcatenationView(clubView, typedResultsView, type);
            case PartitionView typedResultsView -> createPartitionedView(clubView, typedResultsView, type);
            default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
        };
    }


    private Optional<ViewResultTo> createSingleClubViewResult(ClubView clubView, SingleClubView resultsView, EventSupplier eventSupplier) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());

        final var results = eventSupplier.getEvents(club)
                .map(event -> resultListToFactory.createResultList(resultsView, event))
                .toList();

        String key = resultsView.getId().toString() + eventSupplier.getEvents(club).flatMap(event -> {
            return Stream.of(event.getChallengeId(), event.getReferenceId());
        }).distinct().sorted().collect(Collectors.joining("#"));

        ViewResultTo viewResult = new ViewResultTo(
                key,
                resultsView.getName(),
                new ViewPropertiesTo(resultsView.hasPowerStage(), clubView.getBadgeType()),
                results
        );

        return Optional.of(viewResult);
    }

    private Optional<ViewResultTo> createMergedView(ClubView clubView, MergeResultsView mergedResultsView, EventSupplier eventSupplier) {
        if (!hasActiveEvent(eventSupplier, mergedResultsView.getResultViews())) {
            return Optional.empty();
        }

        final var resultViewEventsMap = mergedResultsView.getResultViews().stream()
                .map(resultView -> {
                    final var club = clubSyncService.getOrCreate(resultView.getClubId());

                    List<Event> resultViewEvents = eventSupplier.getEvents(club)
                            .sorted(Comparator.comparing(Event::getReferenceId).reversed())
                            .collect(Collectors.toList());

                    return Pair.of(resultView, resultViewEvents);
                }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        int maximumEventsToJoin = resultViewEventsMap.values().stream().mapToInt(List::size).min().orElseThrow();

        List<ResultListTo> resultListToStream = IntStream.range(0, maximumEventsToJoin).mapToObj(index -> {

            List<ResultListTo> resultLists = resultViewEventsMap.entrySet().stream().map(entrySet -> {

                SingleClubView resultView = entrySet.getKey();
                List<Event> resultViewEvents = entrySet.getValue();

                return resultListToFactory.createResultList(resultView, resultViewEvents.get(index));
            }).collect(Collectors.toList());

            return resultListMerger.mergeResults(resultLists);

        }).toList();


        String key = mergedResultsView.getId().toString() + resultViewEventsMap.values().stream().flatMap(Collection::stream)
                .flatMap(event -> Stream.of(event.getChallengeId(), event.getReferenceId()))
                .distinct()
                .sorted()
                .collect(Collectors.joining("#"));


        ViewResultTo viewResult = new ViewResultTo(
                key,
                mergedResultsView.getName(),
                new ViewPropertiesTo(hasPowerStage(mergedResultsView.getResultViews()), clubView.getBadgeType()),
                resultListToStream
        );

        return Optional.of(viewResult);
    }


    private Optional<ViewResultTo> createConcatenationView(ClubView clubView, ConcatenationView typedResultsView, EventSupplier eventSupplier) {
        if (!hasActiveEvent(eventSupplier, typedResultsView.getResultViews())) {
            return Optional.empty();
        }

        List<ResultListTo> results = typedResultsView.getResultViews().stream().flatMap(resultView -> {
            final var club = clubSyncService.getOrCreate(resultView.getClubId());
            return eventSupplier.getEvents(club).map(event -> {
                return resultListToFactory.createResultList(resultView, event);
            });
        }).collect(Collectors.toList());


        String key = typedResultsView.getId().toString() + results.stream()
                .flatMap(resultList -> resultList.activityInfoTo().stream())
                .flatMap(event -> Stream.of(event.eventChallengeId(), event.eventId()))
                .distinct()
                .sorted()
                .collect(Collectors.joining("#"));

        ViewResultTo viewResult = new ViewResultTo(
                key,
                typedResultsView.getName(),
                new ViewPropertiesTo(hasPowerStage(typedResultsView.getResultViews()), clubView.getBadgeType()),
                results
        );


        return Optional.of(viewResult);
    }

    private Optional<ViewResultTo> createPartitionedView(ClubView clubView, PartitionView typedResultsView, EventSupplier eventSupplier) {
        return createViewResultFromResultsView(clubView, typedResultsView.getResultsView(), eventSupplier).map(resultsView -> {


            return new ViewResultTo(
                    typedResultsView + "#" + resultsView.getViewEventKey(),
                    resultsView.getDescription(),
                    resultsView.getViewPropertiesTo(),
                    resultsView.getMultiListResults().stream().flatMap(resultList -> {
                        return resultListPartitioner.partitionResults(typedResultsView.getPartitionElements(), resultList).stream();
                    }).collect(Collectors.toList())
            );
        });

    }

    private boolean hasActiveEvent(EventSupplier eventSupplier, List<SingleClubView> views) {
        return views.stream()
                .map(SingleClubView::getClubId)
                .map(clubSyncService::getOrCreate)
                .flatMap(eventSupplier::getEvents)
                .findAny().isPresent();
    }

    private boolean hasPowerStage(List<SingleClubView> views) {
        return views.stream().anyMatch(SingleClubView::hasPowerStage);
    }
}
