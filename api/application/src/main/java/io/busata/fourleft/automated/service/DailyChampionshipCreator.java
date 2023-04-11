package io.busata.fourleft.automated.service;

import io.busata.fourleft.domain.options.models.CountryConfiguration;
import io.busata.fourleft.domain.options.models.CountryOption;
import io.busata.fourleft.domain.options.models.StageConditionOption;
import io.busata.fourleft.domain.options.models.VehicleClass;
import io.busata.fourleft.domain.options.models.VehicleClassGroups;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.ServiceArea;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.SurfaceDegradation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateBuilder.championship;
import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateEventBuilder.event;
import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateStageBuilder.stage;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyChampionshipCreator {
    private Random random = new Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));

    private final ClubRepository clubRepository;
    private final CycleOptionsSelector cycleOptionsSelector;

    private final List<VehicleClassGroups> vehicleClassGroupSelection =
            ListUtils.union(
                    Arrays.asList(VehicleClassGroups.values()),
                    List.of(
                            VehicleClassGroups.MODERN,
                            VehicleClassGroups.MODERN,
                            VehicleClassGroups.MODERN_CLASSICS
                    ));

    @Transactional
    public DR2ChampionshipCreateRequestTo createEvent(long clubId) {
        final var countryConfiguration = generatedWeightedCountryConfiguration(clubId);

        final var stages = countryConfiguration.getStages();

        final var stage = stages.get(random.nextInt(stages.size()));

        final var surfaceDegradation = generateSurfaceDegradation();
        final var stageCondition = generateStageCondition(countryConfiguration.getCountry());

        return championship()
                .useHardcoreDamage(true)
                .allowAssists(true)
                .forceCockpitCamera(false)
                .useUnexpectedMoments(false)
                .start(LocalDateTime.now(ZoneOffset.UTC).toString())
                .withEvents(
                        event().country(countryConfiguration.getCountry())
                                .durationMins(calculateDuration())// 24 hours - 10 minutes, gives the bot some time to post results
                                .vehicle(generateWeightedVehicleOption(clubId))
                                .withStages(
                                        stage()
                                                .stageConditionOption(stageCondition)
                                                .degradation(surfaceDegradation)
                                                .serviceArea(ServiceArea.LONG)
                                                .route(stage)
                                )
                )
                .build();
    }

    private static long calculateDuration() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime desired = LocalDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(8).withMinute(50).withSecond(1);

        return Duration.between(now, desired).toMinutes();
    }

    public CountryConfiguration generatedWeightedCountryConfiguration(long clubId) {
        final var club = clubRepository.findByReferenceId(clubId).orElseThrow();

        var countryOptions = club.getChampionships().stream().flatMap(championship -> championship.getEvents().stream())
                .map(Event::getCountry).map(CountryOption::findById).collect(Collectors.toList());

        Collections.reverse(countryOptions);

        return CountryConfiguration.findByCountry(
                cycleOptionsSelector.generate(Arrays.asList(CountryOption.values()), countryOptions)
        );
    }

    public VehicleClass generateWeightedVehicleOption(long clubId) {
        final var club = clubRepository.findByReferenceId(clubId).orElseThrow();
        var previouslyGeneratedVehicles = club.getChampionships().stream().flatMap(championship -> championship.getEvents().stream())
                .map(Event::getVehicleClass).map(VehicleClass::findById).collect(Collectors.toList());

        Collections.reverse(previouslyGeneratedVehicles);

        log.info("Previous vehicles: {}", previouslyGeneratedVehicles.stream().map(VehicleClass::displayName).collect(Collectors.joining(",")));

        final var previouslyGeneratedGroups = previouslyGeneratedVehicles.stream().map(VehicleClassGroups::findGroup).collect(Collectors.toList());

        VehicleClassGroups vehicleGroup = cycleOptionsSelector.generate(vehicleClassGroupSelection,
                previouslyGeneratedGroups);

        return cycleOptionsSelector.generate(vehicleGroup.getVehicleClasses(),
                previouslyGeneratedVehicles.stream().filter(vehicleGroup::containsVehicleOption).collect(Collectors.toList())
        );
    }

    public StageConditionOption generateStageCondition(CountryOption countryOption) {
        int chanceDry = getChanceDryWeather(countryOption);

        int chance = random.nextInt(100);

        if (chance < chanceDry) {
            final var dryConditions = countryOption.getDryConditions();
            return dryConditions.get(random.nextInt(dryConditions.size()));
        } else {
            final var wetConditions = countryOption.getWetConditions();
            return wetConditions.get(random.nextInt(wetConditions.size()));
        }
    }

    public int getChanceDryWeather(CountryOption country) {
        return switch (country) {
            case MONTE_CARLO -> 60;
            case WALES -> 40;
            case GREECE -> 90;
            case GERMANY -> 60;
            case FINLAND -> 60;
            case SWEDEN -> 50;
            case SPAIN -> 90;
            case AUSTRALIA -> 90;
            case NEW_ZEALAND -> 60;
            case ARGENTINA -> 70;
            case POLAND -> 65;
            case USA -> 75;
            case SCOTLAND -> 50;
        };
    }

    public SurfaceDegradation generateSurfaceDegradation() {
        final var options = SurfaceDegradation.values();

        return options[random.nextInt(options.length)];
    }


}
