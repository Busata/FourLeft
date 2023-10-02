package io.busata.fourleft.application.dirtrally2;

import io.busata.fourleft.api.models.CommunityLeaderboardTrackingTo;
import io.busata.fourleft.api.models.CommunityChallengeBoardEntryTo;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.domain.dirtrally2.community.CommunityChallenge;
import io.busata.fourleft.domain.dirtrally2.community.CommunityEvent;
import io.busata.fourleft.domain.dirtrally2.community.CommunityLeaderboardTracking;
import io.busata.fourleft.domain.dirtrally2.community.CommunityStage;
import io.busata.fourleft.domain.dirtrally2.community.CommunityChallengeRepository;
import io.busata.fourleft.domain.dirtrally2.community.CommunityLeaderboardTrackingRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.LeaderboardRepository;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j

@RequiredArgsConstructor
public class CommunityEventService {

    private final CommunityLeaderboardTrackingRepository trackingRepository;
    private final CommunityChallengeRepository challengeRepository;

    private final LeaderboardRepository leaderboardRepository;

    private final PlayerInfoRepository playerInfoRepository;


    @Transactional
    public CommunityLeaderboardTrackingTo trackUser(String racenet) {

        PlayerInfo playerInfo = playerInfoRepository.findByRacenetOrAliases(racenet).orElseThrow();
        playerInfo.setTrackCommunity(true);

        return create(playerInfoRepository.save(playerInfo));
    }

    public List<CommunityLeaderboardTrackingTo> getTrackedUsers() {
        return playerInfoRepository.findTrackedPlayers().stream().map(this::create).toList();
    }

    public CommunityLeaderboardTrackingTo create(PlayerInfo tracking) {
        return new CommunityLeaderboardTrackingTo(tracking.getId(), tracking.getRacenet(), tracking.getDisplayName(), true, true, true, true);
    }

    public void deleteTrackedUser(UUID id) {
        trackingRepository.deleteById(id);
    }

    public List<CommunityChallengeSummaryTo> getResults() {
        return challengeRepository.findBySyncedTrueAndEndedTrue().stream()
                .filter(challenge -> challenge.getEndTime().toLocalDate().equals(LocalDate.now(ZoneId.systemDefault())))
                .map(this::createCommunityChallengeSummary).collect(Collectors.toList());
    }

    private CommunityChallengeSummaryTo createCommunityChallengeSummary( CommunityChallenge challenge) {
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
                .filter(boardEntry -> boardEntry.getPlayerInfo().isTrackCommunity())
                .map(boardEntry -> createEntry(totalRanks, boardEntry, boardEntry.getPlayerInfo().getDisplayName()))
                .collect(Collectors.toList());


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

    public List<CommunityChallengeSummaryTo> getResultsFromYesterday() {
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
                .map(this::createCommunityChallengeSummary).collect(Collectors.toList());
    }
}
