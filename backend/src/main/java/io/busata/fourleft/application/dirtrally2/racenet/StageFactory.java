package io.busata.fourleft.application.dirtrally2.racenet;

import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubResultChampionshipEventStage;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Stage;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Factory
@RequiredArgsConstructor
public class StageFactory {

    public Stage createStage(DR2ClubResultChampionshipEventStage resultStage) {
        Stage stage = new Stage();
        stage.setReferenceId(Long.valueOf(resultStage.id()));
        stage.setName(resultStage.name());
        return stage;
    }
}
