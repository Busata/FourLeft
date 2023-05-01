package io.busata.fourleft.endpoints.aggregators;

import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.application.aggregators.ClubViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClubViewEndpoint {
    private  final ClubViewService clubViewService;

    @GetMapping(RoutesTo.ALL_CLUB_VIEWS)
    public List<ClubViewTo> getClubViews() {
        return clubViewService.getClubViews();
    }
}
