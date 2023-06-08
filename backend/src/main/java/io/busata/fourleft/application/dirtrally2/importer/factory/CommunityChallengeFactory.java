package io.busata.fourleft.application.dirtrally2.importer.factory;

import io.busata.fourleft.domain.dirtrally2.community.CommunityChallenge;
import io.busata.fourleft.domain.dirtrally2.community.CommunityEvent;
import io.busata.fourleft.domain.dirtrally2.community.CommunityStage;
import io.busata.fourleft.common.DR2CommunityEventType;
import io.busata.fourleft.infrastructure.clients.racenet.dto.communityevents.DR2Challenge;
import io.busata.fourleft.infrastructure.common.Factory;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Factory
public class CommunityChallengeFactory {

        public CommunityChallenge updateChallenge(CommunityChallenge challenge, DR2Challenge entry) {
        ZonedDateTime now = ZonedDateTime.now();

        challenge.setChallengeId(entry.id());
        challenge.setVehicleClass(entry.vehicleClass());
        challenge.setDLC(entry.isDirtPlus());

        ZonedDateTime start = ZonedDateTime.parse(entry.entryWindow().start());
        ZonedDateTime end = ZonedDateTime.parse(entry.entryWindow().end());

        challenge.setStartTime(start);
        challenge.setEndTime(end);
        challenge.setEnded(end.isBefore(now));

        challenge.updateEvents(entry.events().stream().map(eventEntry -> {
            CommunityEvent event = new CommunityEvent();
            event.setEventId(eventEntry.id());
            event.setDiscipline(eventEntry.discipline());
            event.setName(eventEntry.name());
            event.setChallenge(challenge);

            event.updateStages(eventEntry.stages().stream().map(stageEntry -> {
                CommunityStage stage = new CommunityStage();
                stage.setEvent(event);
                stage.setStageId(stageEntry.id());
                stage.setName(stageEntry.name());
                stage.setCountry(stageEntry.country());
                stage.setLocation(stageEntry.location());
                return stage;
            }).collect(Collectors.toList()));

            return event;
        }).collect(Collectors.toList()));

        return challenge;
    }

    public CommunityChallenge createChallenge(DR2CommunityEventType type, DR2Challenge entry) {
        CommunityChallenge challenge = new CommunityChallenge();
        challenge.setType(type);
        return updateChallenge(challenge, entry);
    }
}
