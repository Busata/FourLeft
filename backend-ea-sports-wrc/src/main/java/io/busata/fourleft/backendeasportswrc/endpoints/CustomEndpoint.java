package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.ClubOverviewTo;
import io.busata.fourleft.backendeasportswrc.domain.services.CustomOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomEndpoint {

    private final CustomOverviewService customOverviewService;

    @GetMapping("/api_v2/custom/maint_masters")
    public ClubOverviewTo getMainMastersSummary() {

        return customOverviewService.createOverview("450");

    }

    @GetMapping("/api_v2/custom/club_summary/{clubId}")
    public ClubOverviewTo getClubSummary(@PathVariable String clubId) {

        return customOverviewService.createOverview(clubId);

    }

}
