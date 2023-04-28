package io.busata.fourleft.racenet.factory;

import io.busata.fourleft.racenet.dto.club.DR2ClubResultChampionshipEventStage;
import io.busata.fourleft.domain.clubs.models.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StageFactory {

    public Stage createStage(DR2ClubResultChampionshipEventStage resultStage) {
        Stage stage = new Stage();
        stage.setReferenceId(Long.valueOf(resultStage.id()));
        stage.setName(resultStage.name());
        return stage;
    }
}
