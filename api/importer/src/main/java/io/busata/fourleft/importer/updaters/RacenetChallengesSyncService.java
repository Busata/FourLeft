package io.busata.fourleft.importer.updaters;

import io.busata.fourleft.common.TransactionHandler;
import io.busata.fourleft.domain.challenges.models.CommunityChallenge;
import io.busata.fourleft.domain.challenges.models.CommunityEvent;
import io.busata.fourleft.domain.challenges.models.CommunityStage;
import io.busata.fourleft.domain.clubs.models.DR2CommunityEventType;
import io.busata.fourleft.gateway.racenet.factory.CommunityChallengeFactory;
import io.busata.fourleft.domain.challenges.repository.CommunityChallengeRepository;
import io.busata.fourleft.gateway.racenet.RacenetGateway;
import io.busata.fourleft.gateway.racenet.dto.communityevents.DR2Challenge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RacenetChallengesSyncService {
    private final TransactionHandler transactionHandler;

    private final RacenetGateway client;
    private final CommunityChallengeRepository challengeRepository;
    private final CommunityChallengeFactory communityChallengeFactory;
    private final LeaderboardFetcher leaderboardFetcher;

    public void syncWithRacenet() {
        transactionHandler.runInTransaction(this::updateCommunityEvents);
        this.updateLeaderboards();
    }

    private void updateLeaderboards() {
        challengeRepository.findBySyncedFalseAndEndedTrue().forEach(communityChallenge -> {
            try {
                updateChallenge(communityChallenge.getId());
            } catch (Exception ex) {
                log.error("Something went wrong updating a challenge, will be synced later", ex);
            }
        });
    }

    public void updateCommunityEvents() {
        client.getCommunityEvents()
                .forEach(communityEvent -> communityEvent.challengeGroups()
                        .forEach(challengeGroup -> challengeGroup.challenges()
                                .forEach(challenge -> {
                                    this.upsertCommunityChallenge(communityEvent.type(), challenge);
                                })));
    }

    private void upsertCommunityChallenge(DR2CommunityEventType type, DR2Challenge entry) {
        challengeRepository.findByChallengeId(entry.id()).ifPresentOrElse((challenge) -> {
            if (!challenge.isEnded()) {
                log.info("-- Challenge not ended yet, updating");
                communityChallengeFactory.updateChallenge(challenge, entry);
                challengeRepository.save(challenge);
            }
        }, () -> {
            log.info("Creating new challenge");
            CommunityChallenge challenge = communityChallengeFactory.createChallenge(type, entry);
            challengeRepository.save(challenge);
        });
    }

    private void updateChallenge(UUID id) {
        transactionHandler.runInTransaction(() -> {
            CommunityChallenge communityChallenge = challengeRepository.getById(id);
            updateLeaderboard(communityChallenge);
            communityChallenge.setSynced(true);
            challengeRepository.save(communityChallenge);
        });
    }

    private void updateLeaderboard(CommunityChallenge communityChallenge) {
        CommunityEvent lastEvent = communityChallenge.getEvents().stream().sorted(Comparator.comparing(CommunityEvent::getEventId)).toList().get(communityChallenge.getEvents().size() - 1);
        CommunityStage lastStage = lastEvent.getStages().stream().sorted(Comparator.comparing(CommunityStage::getStageId)).toList().get(lastEvent.getStages().size() - 1);

        String challengeId = communityChallenge.getChallengeId();
        String eventId = lastEvent.getEventId();
        String stageId = lastStage.getStageId();

        leaderboardFetcher.upsertBoard(challengeId, eventId, stageId);
    }

}
