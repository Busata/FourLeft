package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.results_views.MergedView;
import io.busata.fourleft.domain.configuration.results_views.MultipleClubsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import io.busata.fourleft.endpoints.views.ClubEventSupplier;
import io.busata.fourleft.endpoints.views.ClubEventSupplierType;
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

    @Transactional
    @Cacheable("view_results")
    public Optional<ViewResultTo> createViewResult(UUID viewId, ClubEventSupplierType type) {
        /*
           CREST
           2 clubs
           2 events

           Tiers + Merge: how?


           create Player restrictions (CA 1, CA 2, CA 3, CA 4 with type INCLUDE)


            Create Single club view for club A
            Create single club view for club B

            create MergedView, with the two single club views.


            A view should be able to take any* view as input.








           Single club view per tier x 2?




         */
        return clubViewRepository.findById(viewId).flatMap(clubView -> {
            return switch (clubView.getResultsView()) {
                case SingleClubView view -> createSingleClubViewResult(view, type.getSupplier());
                case TieredView view -> createTieredViewResult(view, type.getSupplier());
                case MergedView view -> createMergedViewResult(view, type.getSupplier());
                default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
            };
        });
    }

    public Optional<ViewResultTo> createSingleClubViewResult(SingleClubView view, ClubEventSupplier eventSupplier) {
        final var club = clubSyncService.getOrCreate(view.getClubId());

        final var results = eventSupplier.getEvent(club)
                .map(event -> resultListToFactory.createResultList(view, event))
                .stream()
                .toList();

        ViewResultTo viewResult = new ViewResultTo(
                view.getName(),
                new ViewPropertiesTo(view.isPowerStage(), view.getBadgeType()),
                results
        );

        return Optional.of(viewResult);
    }

    private Optional<ViewResultTo> createTieredViewResult(TieredView view, ClubEventSupplier supplier) {
        return createConcatenation(view, supplier);
    }
    private Optional<ViewResultTo> createMergedViewResult(MergedView view, ClubEventSupplier eventSupplier) {
        return createConcatenation(view, eventSupplier).map(viewResultTo -> {

            return new ViewResultTo(
                    view.getName(),
                    new ViewPropertiesTo(view.isPowerStage(), view.getBadgeType()),
                    resultListToFactory.mergeResultLists(viewResultTo.getMultiListResults())
            );
        });
    }
    private Optional<ViewResultTo> createConcatenation(MultipleClubsView tieredView, ClubEventSupplier eventSupplier) {

        if (!hasActiveEvent(eventSupplier, tieredView.getResultViews())) {
            return Optional.empty();
        }

        final var results = tieredView.getResultViews().stream()
                .map(resultViews -> {
                    final var club = clubSyncService.getOrCreate(resultViews.getClubId());

                    return eventSupplier.getEvent(club)
                            .map(event -> resultListToFactory.createResultList(resultViews, event))
                            .orElseThrow();
                }).collect(Collectors.toList());


        ViewResultTo viewResult = new ViewResultTo(
                tieredView.getName(),
                new ViewPropertiesTo(tieredView.isPowerStage(), tieredView.getBadgeType()),
                results
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
}
