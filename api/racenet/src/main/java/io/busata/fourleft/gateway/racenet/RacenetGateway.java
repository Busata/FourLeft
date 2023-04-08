package io.busata.fourleft.gateway.racenet;

import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubDetails;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubMembers;
import io.busata.fourleft.gateway.racenet.dto.club.DR2ClubRecentResults;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreationStatus;
import io.busata.fourleft.gateway.racenet.dto.club.championship.standings.DR2ChampionshipStandings;
import io.busata.fourleft.gateway.racenet.dto.communityevents.DR2CommunityEvent;
import io.busata.fourleft.gateway.racenet.dto.leaderboard.DR2LeaderboardRequest;
import io.busata.fourleft.gateway.racenet.dto.leaderboard.DR2LeaderboardResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Supplier;


@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetGateway {

    private final RacenetAuthorization authorization;
    private final RacenetApi api;

    @PostConstruct
    public void afterConstruction() {
        //authorization.refreshLogin();
    }

    public DR2ChampionshipStandings getClubChampionshipStandings(long clubId, int page) {
        return doAuthorizedCall(() -> api.getStandings(authorization.getHeaders(), clubId, page, 100));
    }

    public DR2LeaderboardResults getLeaderboard(DR2LeaderboardRequest request) {
        return doAuthorizedCall(() -> api.getLeaderboard(authorization.getHeaders(), request));
    }

    public DR2ClubRecentResults getClubRecentResults(long clubId) {
        return doAuthorizedCall(() -> api.getClubRecentResults(authorization.getHeaders(), clubId));
    }

    public List<DR2ClubChampionships> getChampionships(long clubId) {
        return doAuthorizedCall(() -> api.getClubChampionships(authorization.getHeaders(), clubId));
    }

    public DR2ClubDetails getClubDetails(long clubId) {
        return doAuthorizedCall(() -> api.getClubDetails(authorization.getHeaders(), clubId));
    }

    public DR2ClubMembers getClubMembers(long clubId, int pageSize, int page) {
        Assert.isTrue(pageSize <= 200, "Page size max 200");
        return doAuthorizedCall(() -> api.getMembers(authorization.getHeaders(), clubId, pageSize, page));
    }

    public DR2ChampionshipCreationStatus createChampionship(long clubId, DR2ChampionshipCreateRequestTo create) {
        return doAuthorizedCall(() -> api.createChampionship(authorization.getHeaders(), clubId, create));
    }

    public List<DR2CommunityEvent> getCommunityEvents() {
        return doAuthorizedCall(() -> api.getCommunity(authorization.getHeaders()));
    }

    private <T> T doAuthorizedCall(Supplier<T> supplier) {
        authorization.refreshLogin();
        return supplier.get();
    }
}
