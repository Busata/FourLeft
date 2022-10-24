package io.busata.fourleft.endpoints.community;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.domain.challenges.models.CommunityChallenge;
import io.busata.fourleft.domain.challenges.models.CommunityEvent;
import io.busata.fourleft.domain.challenges.models.CommunityStage;
import io.busata.fourleft.api.models.CommunityChallengeBoardEntryTo;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.api.models.TrackUserRequestTo;
import io.busata.fourleft.domain.challenges.models.CommunityLeaderboardTracking;
import io.busata.fourleft.domain.challenges.repository.CommunityChallengeRepository;
import io.busata.fourleft.domain.challenges.repository.CommunityLeaderboardTrackingRepository;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequiredArgsConstructor
public class CommunityEventsEndpoint {

    private final CommunityLeaderboardTrackingRepository trackingRepository;
    private final CommunityChallengeRepository challengeRepository;
    private final LeaderboardRepository leaderboardRepository;

    @PostMapping(Routes.COMMUNITY_TRACK_USER)
    CommunityLeaderboardTracking trackUserRequest(@RequestBody TrackUserRequestTo userRequestTo) {
        CommunityLeaderboardTracking tracking = new CommunityLeaderboardTracking();
        log.info("{} has requested to be tracked for results", userRequestTo.nickName());
        tracking.setNickName(userRequestTo.nickName());
        tracking.setAlias(userRequestTo.alias());

        return trackingRepository.save(tracking);
    }

    @GetMapping(Routes.GET_TRACKED_USERS)
    public List<CommunityLeaderboardTracking> getTrackedUsers() {
        return trackingRepository.findAll();
    }

    @DeleteMapping(Routes.GET_TRACKED_USER_BY_ID)
    public void deleteUserById(@PathVariable UUID id) {
        trackingRepository.deleteById(id);
    }


    @GetMapping(Routes.COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getResults() {
        final var trackedUsers = trackingRepository.findAll();
        return challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault())))
                .map(challenge -> createCommunityChallengeSummary(trackedUsers, challenge)).collect(Collectors.toList());
    }

    @GetMapping(Routes.PREVIOUS_COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getResultsFromYesterday() {
        final var trackedUsers = trackingRepository.findAll();
        return challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault()).minusDays(1)))
                .filter(challenge -> {
                    boolean present = challenge.getLeaderboardKey().flatMap(leaderboardRepository::findLeaderboard).isPresent();
                    if(!present) {
                        log.warn("A challenge was not included, type ({}), vehicle({}), challengeId({})", challenge.getType(), challenge.getVehicleClass(), challenge.getChallengeId());
                    }
                    return present;
                })
                .map(challenge -> createCommunityChallengeSummary(trackedUsers, challenge)).collect(Collectors.toList());
    }

    private CommunityChallengeSummaryTo createCommunityChallengeSummary(List<CommunityLeaderboardTracking> trackedUsers, CommunityChallenge challenge) {
        final var leaderboard = challenge.getLeaderboardKey()
                .flatMap(leaderboardRepository::findLeaderboard)
                .orElseThrow();

        int totalRanks = leaderboard.getEntries().size();




        final var sortedEntries = leaderboard
                .getEntries()
                .stream()
                .sorted(Comparator.comparing(BoardEntry::getRank)).toList();

        final var onePercentRank = Math.floor(totalRanks * 0.01) - 1;
        final var topOnePercentEntry = sortedEntries.get((int) onePercentRank);

        List<CommunityChallengeBoardEntryTo> entries = sortedEntries.stream()
                .filter(boardEntry -> trackedUsers.stream().anyMatch(trackedUser -> trackedUser.getAlias().equalsIgnoreCase(boardEntry.getName())))
                .map(boardEntry -> {
                    final var trackedUser = trackedUsers.stream().filter(user -> user.getAlias().equalsIgnoreCase(boardEntry.getName())).findFirst().orElseThrow();
                    return createEntry(totalRanks, boardEntry, trackedUser.getNickName());
                }).collect(Collectors.toList());


        return new CommunityChallengeSummaryTo(
                challenge.getType(),
                challenge.getVehicleClass(),
                challenge.getEvents().stream().map(CommunityEvent::getName).map(this::mapToCountry).toList(),
                challenge.getLastEvent().flatMap(CommunityEvent::getLastStage).map(CommunityStage::getName).orElse(""),
                createEntry(totalRanks, topOnePercentEntry),
                createEntry(totalRanks, sortedEntries.get(0)),
                entries
        );
    }

    private CommunityChallengeBoardEntryTo createEntry(int totalRanks, BoardEntry boardEntry, String alias) {
        return new CommunityChallengeBoardEntryTo(
                boardEntry.getRank(),
                alias,
                boardEntry.getNationality(),
                boardEntry.getVehicleName(),
                boardEntry.getTotalTime(),
                boardEntry.getTotalDiff(),
                totalRanks,
                ((float) boardEntry.getRank() / (float) totalRanks) * 100f,
                boardEntry.isDnf()
        );
    }

    private CommunityChallengeBoardEntryTo createEntry(int totalRanks, BoardEntry boardEntry) {
        return createEntry(totalRanks, boardEntry, boardEntry.getName());
    }

    private String mapToCountry(String eventName) {
        return switch (eventName) {
            case "HAWKES BAY" -> "eNewZealand";
            case "RIBADELLES", "CIRCUIT DE BARCELONA-CATALUNYA" -> "eSpain";
            case "MONARO" -> "eAustralia";
            case "TROIS-RIVIÈRES" -> "eCanada";
            case "MONTALEGRE" -> "ePortugal";
            case "LOHÉAC, BRETAGNE" -> "eFrance";
            case "HÖLJES" -> "eSweden";
            case "NEW ENGLAND" -> "eUsa";
            case "METTET" -> "eBelgium";
            case "ŁĘCZNA COUNTY" -> "ePoland";
            case "SILVERSTONE" -> "eEngland";
            case "CATAMARCA PROVINCE" -> "eArgentina";
            default -> eventName;
        };
    }

}

