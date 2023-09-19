package io.busata.fourleft.infrastructure.clients.racenet;

import io.busata.fourleft.infrastructure.clients.racenet.dto.communityevents.DR2CommunityEvent;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubDetails;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubMembers;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubRecentResults;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreationStatus;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.standings.DR2ChampionshipStandings;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardRequest;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetGateway {

    private final RacenetAuthorizationApi authorizationApi;
    private final RacenetApi api;

    public DR2ChampionshipStandings getClubChampionshipStandings(long clubId, int page) {
        return doAuthorizedCall((headers) -> api.getStandings(headers, clubId, page, 100));
    }

    public DR2LeaderboardResults getLeaderboard(DR2LeaderboardRequest request) {
        return doAuthorizedCall((headers) -> api.getLeaderboard(headers, request));
    }

    public DR2ClubRecentResults getClubRecentResults(long clubId) {
        return doAuthorizedCall((headers) -> api.getClubRecentResults(headers, clubId));
    }

    public List<DR2ClubChampionships> getChampionships(long clubId) {
        return doAuthorizedCall((headers) -> api.getClubChampionships(headers, clubId));
    }

    public DR2ClubDetails getClubDetails(long clubId) {
        return doAuthorizedCall((headers) -> api.getClubDetails(headers, clubId));
    }

    public DR2ClubMembers getClubMembers(long clubId, int pageSize, int page) {
        Assert.isTrue(pageSize <= 200, "Page size max 200");
        return doAuthorizedCall((headers) -> api.getMembers(headers, clubId, pageSize, page));
    }

    public DR2ChampionshipCreationStatus createChampionship(long clubId, DR2ChampionshipCreateRequestTo create) {
        return doAuthorizedCall((headers) -> api.createChampionship(headers, clubId, create));
    }

    public List<DR2CommunityEvent> getCommunityEvents() {
        return doAuthorizedCall((headers) -> api.getCommunity(headers));
    }

    private <T> T doAuthorizedCall(Function<HttpHeaders, T> supplier) {
        return supplier.apply(getHeaders());
    }

    public HttpHeaders getHeaders() {
        Map<String, String> headers = authorizationApi.getHeaders();

        return new HttpHeaders();
    }
}
