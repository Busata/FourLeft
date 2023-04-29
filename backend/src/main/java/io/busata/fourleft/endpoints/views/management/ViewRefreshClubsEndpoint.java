package io.busata.fourleft.endpoints.views.management;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.messages.LeaderboardUpdated;
import io.busata.fourleft.domain.views.configuration.ClubViewRepository;
import io.busata.fourleft.application.dirtrally2.importer.ClubSyncService;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.application.dirtrally2.importer.updaters.RacenetSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ViewRefreshClubsEndpoint {
    private final ClubSyncService clubSyncService;
    private final ClubViewRepository clubViewRepository;

    private final RacenetSyncService racenetSyncService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping(Routes.CLUB_VIEWS_REFRESH)
    public void forceRefresh(@PathVariable UUID viewId) {
        clubViewRepository.findById(viewId).ifPresent(clubView -> {
            clubView.getResultsView().getAssociatedClubs().forEach(clubId -> {
                Club club = clubSyncService.getOrCreate(clubId);
                racenetSyncService.fullRefreshClub(club.getReferenceId());
                eventPublisher.publishEvent(new LeaderboardUpdated(clubId));
            });
        });


    }

}
