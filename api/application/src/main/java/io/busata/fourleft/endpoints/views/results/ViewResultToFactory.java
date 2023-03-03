package io.busata.fourleft.endpoints.views.results;

import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import io.busata.fourleft.endpoints.views.ClubEventSupplier;
import io.busata.fourleft.endpoints.views.ClubEventSupplierType;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ViewResultToFactory {
    private final ClubSyncService clubSyncService;
    private final ClubViewRepository clubViewRepository;
    private final SingleListResultToFactory singleListResultToFactory;

    @Transactional
    @Cacheable("view_results")
    public Optional<ViewResultTo> createViewResult(UUID viewId, ClubEventSupplierType type) {
        return clubViewRepository.findById(viewId).flatMap(clubView -> {
            return switch (clubView.getResultsView()) {
                case SingleClubView view -> createSingleClubViewResult(view, type.getSupplier());
                case TieredView view -> createdTieredViewResult(view, type.getSupplier());
                default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
            };
        });
    }

    public Optional<ViewResultTo> createSingleClubViewResult(SingleClubView view, ClubEventSupplier eventSupplier) {
        final var club = clubSyncService.getOrCreate(view.getClubId());

        final var results = eventSupplier.getEvent(club)
                .map(event -> singleListResultToFactory.createSingleResultList(view, event))
                .stream()
                .toList();

        ViewResultTo viewResult = new ViewResultTo(
                view.getName(),
                new ViewPropertiesTo(view.isUsePowerStage(), view.getBadgeType()),
                results
        );

        return Optional.of(viewResult);
    }

    private Optional<ViewResultTo> createdTieredViewResult(TieredView tieredView, ClubEventSupplier eventSupplier) {

        if (!hasActiveEvent(eventSupplier, tieredView)) {
            return Optional.empty();
        }

        final var results = tieredView.getResultViews().stream()
                .map(resultViews -> {
                    final var club = clubSyncService.getOrCreate(resultViews.getClubId());

                    return eventSupplier.getEvent(club)
                            .map(event -> singleListResultToFactory.createSingleResultList(resultViews, event))
                            .orElseThrow();
                }).collect(Collectors.toList());


        ViewResultTo viewResult = new ViewResultTo(
                tieredView.getName(),
                new ViewPropertiesTo(tieredView.isUsePowerStage(), tieredView.getBadgeType()),
                results
        );

        return Optional.of(viewResult);
    }

    private boolean hasActiveEvent(ClubEventSupplier eventSupplier, TieredView tieredView) {
        return tieredView.getResultViews().stream()
                .map(SingleClubView::getClubId)
                .map(clubSyncService::getOrCreate)
                .map(eventSupplier::getEvent)
                .anyMatch(Optional::isPresent);
    }
}
