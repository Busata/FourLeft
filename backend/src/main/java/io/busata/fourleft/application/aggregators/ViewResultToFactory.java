package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.aggregators.ClubView;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.domain.aggregators.results.ConcatenationView;
import io.busata.fourleft.domain.aggregators.results.MergeResultsView;
import io.busata.fourleft.domain.aggregators.results.PartitionView;
import io.busata.fourleft.domain.aggregators.results.ResultsView;
import io.busata.fourleft.domain.aggregators.results.SingleClubView;
import io.busata.fourleft.domain.aggregators.EventSupplier;
import io.busata.fourleft.application.dirtrally2.ClubService;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Factory
@RequiredArgsConstructor
public class ViewResultToFactory {
    private final ClubService clubService;
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
        if (resultsView instanceof SingleClubView typedResultsView) {
            return createSingleClubViewResult(clubView, typedResultsView, type);
        } else if (resultsView instanceof MergeResultsView typedResultsView) {
            return createMergedView(clubView, typedResultsView, type);
        } else if (resultsView instanceof ConcatenationView typedResultsView) {
            return createConcatenationView(clubView, typedResultsView, type);
        } else if (resultsView instanceof PartitionView typedResultsView) {
            return createPartitionedView(clubView, typedResultsView, type);
        }
        throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
    }


    private Optional<ViewResultTo> createSingleClubViewResult(ClubView clubView, SingleClubView resultsView, EventSupplier eventSupplier) {
        final var club = clubService.getOrCreate(resultsView.getClubId());

        final var results = eventSupplier.getEvents(club)
                .map(event -> resultListToFactory.createResultList(resultsView, event))
                .toList();

        String key = eventSupplier.getEvents(club).flatMap(event -> {
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
                    final var club = clubService.getOrCreate(resultView.getClubId());

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

            return resultListMerger.mergeResults(resultLists, mergedResultsView.getMergeMode(), mergedResultsView.getRacenetFilter());

        }).toList();


        String key = resultViewEventsMap.values().stream().flatMap(Collection::stream)
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
            final var club = clubService.getOrCreate(resultView.getClubId());
            return eventSupplier.getEvents(club).map(event -> {
                return resultListToFactory.createResultList(resultView, event);
            });
        }).collect(Collectors.toList());


        String key = results.stream()
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
                    resultsView.getViewEventKey(),
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
                .map(clubService::getOrCreate)
                .flatMap(eventSupplier::getEvents)
                .findAny().isPresent();
    }

    private boolean hasPowerStage(List<SingleClubView> views) {
        return views.stream().anyMatch(SingleClubView::hasPowerStage);
    }
}
