package io.busata.fourleft.endpoints.club.automated.service;

import io.busata.fourleft.domain.options.models.CountryOption;
import io.busata.fourleft.domain.options.models.StageOption;
import io.busata.fourleft.domain.options.models.VehicleClass;
import io.busata.fourleft.gateway.racenet.RacenetGateway;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateEventBuilder;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateStageBuilder;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.SurfaceDegradation;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateBuilder.championship;
import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateEventBuilder.event;
import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateStageBuilder.stage;

@Component
@RequiredArgsConstructor
public class ClubChampionshipCopier {
    private Random random = new Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    private final ClubSyncService clubSyncService;
    private final RacenetGateway client;

    public DR2ChampionshipCreateRequestTo createCopyTemplate(long fromClubId) {
        final var fromClub = clubSyncService.getOrCreate(fromClubId);

        return championship()
                        .start(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5).toString())
                        .withEvents(fromClub.getCurrentEvent().stream().map(fromEvent -> {
                            CountryOption country = CountryOption.findById(fromEvent.getCountry());
                            return event()
                                    .country(country)
                                    .durationMins(60)
                                    .vehicle(VehicleClass.findById(fromEvent.getVehicleClass()))
                                    .withStages(
                                            fromEvent.getStages().stream().map(fromStage -> {
                                                final var dryConditions = country.getDryConditions();

                                                return stage()
                                                        .stageConditionOption(dryConditions.get(random.nextInt(dryConditions.size())))
                                                        .degradation(SurfaceDegradation.MEDIUM)
                                                        .route(StageOption.findByName(fromStage.getName()));
                                            }).toArray(DR2ChampionshipCreateStageBuilder[]::new)
                                    );
                        }).toArray(DR2ChampionshipCreateEventBuilder[]::new)).build();


    }
}
