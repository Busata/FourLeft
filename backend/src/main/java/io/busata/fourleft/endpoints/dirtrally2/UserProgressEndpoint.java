package io.busata.fourleft.endpoints.dirtrally2;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.overview.ClubResultSummaryTo;
import io.busata.fourleft.api.models.overview.CommunityResultSummaryTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.application.dirtrally2.UserOverviewService;
import io.busata.fourleft.application.dirtrally2.UserProgressImageService;
import io.busata.fourleft.domain.dirtrally2.challenges.models.CommunityChallenge;
import io.busata.fourleft.domain.dirtrally2.challenges.models.CommunityEvent;
import io.busata.fourleft.domain.dirtrally2.challenges.repository.CommunityChallengeRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.models.BoardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.domain.aggregators.ClubEventSupplier;
import io.busata.fourleft.application.aggregators.ViewResultToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserProgressEndpoint {

    private final UserProgressImageService userProgressImageService;
    private final UserOverviewService userOverviewService;


    @GetMapping(value = Routes.USER_COMMUNITY_PROGRESSION, produces = "image/png")
    public BufferedImage calculateUser(@RequestParam String query,
                                       @RequestParam(required = false, defaultValue = "false") boolean includeName,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> before,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  Optional<LocalDate> after) {
        return userProgressImageService.createImage(query, includeName, before, after);
    }

    @GetMapping(Routes.USER_OVERVIEW)
    public UserResultSummaryTo getUserOverview(@RequestParam String query) {
        return  userOverviewService.getUserOverview(query);
    }


}
