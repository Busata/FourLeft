package io.busata.fourleft.endpoints.dirtrally2;

import io.busata.fourleft.api.models.CommunityLeaderboardTrackingTo;
import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.api.models.TrackUserRequestTo;
import io.busata.fourleft.application.dirtrally2.CommunityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@Slf4j
@RequiredArgsConstructor
public class CommunityEventsEndpoint {
    private final CommunityEventService communityEventService;

    @PostMapping(Routes.COMMUNITY_TRACK_USER)
    CommunityLeaderboardTrackingTo trackUserRequest(@RequestBody TrackUserRequestTo userRequestTo) {
        return communityEventService.trackUser(userRequestTo.nickName(), userRequestTo.alias());
    }

    @GetMapping(Routes.GET_TRACKED_USERS)
    public List<CommunityLeaderboardTrackingTo> getTrackedUsers() {
        return communityEventService.getTrackedUsers();
    }

    @DeleteMapping(Routes.GET_TRACKED_USER_BY_ID)
    public void deleteUserById(@PathVariable UUID id) {
        communityEventService.deleteTrackedUser(id);
    }


    @GetMapping(Routes.COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getResults() {
        return communityEventService.getResults();
    }

    @GetMapping(Routes.PREVIOUS_COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getResultsFromYesterday() {
        return communityEventService.getResultsFromYesterday();
    }


}

