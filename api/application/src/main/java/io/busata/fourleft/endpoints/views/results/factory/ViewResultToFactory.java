package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.results_views.*;
import io.busata.fourleft.endpoints.views.ClubEventSupplier;
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
    private final ClubViewRepository clubViewRepository;
    private final ResultListToFactory resultListToFactory;

    private final ResultListMerger resultListMerger;

    private final ResultListPartitioner resultListPartitioner;

    @Transactional
    @Cacheable("view_results")
    public Optional<ViewResultTo> createViewResult(UUID viewId, ClubEventSupplier type) {
        return clubViewRepository.findById(viewId).flatMap(clubView -> {
            return createViewResultFromResultsView(clubView, clubView.getResultsView(), type);
        });
    }

    private Optional<ViewResultTo> createViewResultFromResultsView(ClubView clubView, ResultsView resultsView, ClubEventSupplier type) {
        return switch (resultsView) {
            case SingleClubView typedResultsView -> createSingleClubViewResult(clubView, typedResultsView, type);
            case PartitionView typedResultsView -> createPartitionedView(clubView, typedResultsView, type);
            case MergeResultsView typedResultsView -> createMergedView(clubView, typedResultsView, type);
            default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
        };
    }

    public Optional<ViewResultTo> createSingleClubViewResult(ClubView clubView, SingleClubView resultsView, ClubEventSupplier eventSupplier) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());

        final var results = eventSupplier.getEvent(club)
                .map(event -> resultListToFactory.createResultList(resultsView, event))
                .stream()
                .toList();

        ViewResultTo viewResult = new ViewResultTo(
                resultsView.getName(),
                new ViewPropertiesTo(resultsView.hasPowerStage(), clubView.getBadgeType()),
                results
        );

        return Optional.of(viewResult);
    }

    private Optional<ViewResultTo> createPartitionedView(ClubView clubView, PartitionView typedResultsView, ClubEventSupplier eventSupplier) {

        return createViewResultFromResultsView(clubView,typedResultsView.getResultsView(), eventSupplier).map(resultsView -> {
            if(resultsView.getMultiListResults().size() != 1) {
                throw new IllegalStateException("Not expecting multiple lists for this view");
            }

            final var resultList = resultsView.getMultiListResults().get(0);

            return new ViewResultTo(
                    resultsView.getDescription(),
                    resultsView.getViewPropertiesTo(),
                    resultListPartitioner.partitionResults(typedResultsView.getPartitionElements(), resultList)
            );
        });

    }

    private Optional<ViewResultTo> createMergedView(ClubView clubView, MergeResultsView typedResultsView, ClubEventSupplier eventSupplier) {

        if(!hasActiveEvent(eventSupplier, typedResultsView.getResultViews())) {
            return Optional.empty();
        }

        final var results = typedResultsView.getResultViews().stream()
                .map(resultViews -> {
                    final var club = clubSyncService.getOrCreate(resultViews.getClubId());

                    return eventSupplier.getEvent(club)
                            .map(event -> resultListToFactory.createResultList(resultViews, event))
                            .orElseThrow();
                }).collect(Collectors.toList());


        ResultListTo mergedResultList = resultListMerger.mergeResults(results);

        ViewResultTo viewResult = new ViewResultTo(
                typedResultsView.getName(),
                new ViewPropertiesTo(hasPowerStage(typedResultsView.getResultViews()), clubView.getBadgeType()),
                List.of(mergedResultList)
        );

        return Optional.of(viewResult);

    }

    private boolean hasActiveEvent(ClubEventSupplier eventSupplier, List<SingleClubView> views) {
        return views.stream()
                .map(SingleClubView::getClubId)
                .map(clubSyncService::getOrCreate)
                .map(eventSupplier::getEvent)
                .anyMatch(Optional::isPresent);
    }
    private boolean hasPowerStage(List<SingleClubView> views) {
        return views.stream().anyMatch(SingleClubView::hasPowerStage);
    }
}
