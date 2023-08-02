package io.busata.fourleft.application.dirtrally2.championshipcreator;

import com.google.common.collect.Lists;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.Stage;
import io.busata.fourleft.domain.dirtrally2.options.CountryOption;
import io.busata.fourleft.domain.dirtrally2.options.StageConditionOption;
import io.busata.fourleft.domain.dirtrally2.options.StageOption;
import io.busata.fourleft.domain.dirtrally2.options.VehicleClass;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateStageBuilder;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.ServiceArea;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.SurfaceDegradation;
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
import java.util.Set;
import java.util.stream.Collectors;

import static io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateBuilder.championship;
import static io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateEventBuilder.event;
import static io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation.DR2ChampionshipCreateStageBuilder.stage;


@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyChampionshipCreator {
    private final Random random = new Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    private final ClubRepository clubRepository;
    private final CycleOptionsSelector cycleOptionsSelector;
    private final int COUNTRY_CYCLE_LENGTH = 4;

    @Transactional
    public DR2ChampionshipCreateRequestTo createEvent(long clubId) {
        CountryOption countryOption = generateCountry(clubId);
        VehicleClass vehicleClass = generateVehicle();

        List<DR2ChampionshipCreateStageBuilder> stages = generateStages(clubId, countryOption);

        return championship()
                .useHardcoreDamage(true)
                .allowAssists(true)
                .forceCockpitCamera(false)
                .useUnexpectedMoments(false)
                .start(LocalDateTime.now(ZoneOffset.UTC).toString())
                .withEvents(
                        event().country(countryOption)
                                .durationMins(calculateDuration())
                                .vehicle(vehicleClass)
                                .withStages(
                                        stages.stream().toArray(DR2ChampionshipCreateStageBuilder[]::new)
                                )
                )
                .build();
    }

    private static long calculateDuration() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime desired = LocalDateTime.now(ZoneOffset.UTC).plusWeeks(1).withHour(6).withMinute(50).withSecond(0);

        return Duration.between(now, desired).toMinutes();
    }

    private List<DR2ChampionshipCreateStageBuilder> generateStages(long clubId, CountryOption countryOption) {
        final var uniqueStageOptions = Arrays.asList(StageOption.values()).stream().filter(stageOption -> stageOption.country().equals(countryOption)).toList();

        final var previousStages = getLastGeneratedEvents(clubId).stream().flatMap(event -> event.getStages().stream()).map(Stage::getName)
                .map(StageOption::findByName)
                .filter(stageOption -> stageOption.country().equals(countryOption))
                .toList();

        final var remainingLongs = ListUtils.subtract(
                uniqueStageOptions.stream().filter(StageOption::isLong).toList(),
                previousStages.stream().filter(StageOption::isLong).limit(4).toList()
                );

        List<StageOption> remainingLongStages = remainingLongs.stream().filter(StageOption::isLong).toList();
        StageOption longStage = remainingLongStages.get(random.nextInt(remainingLongStages.size()));

        final var remainingShorts = ListUtils.subtract(
                uniqueStageOptions.stream().filter(StageOption::isShort).toList(),
                previousStages.stream().filter(StageOption::isShort).limit(5).toList()
        );

        StageOption shortStageA = remainingShorts.get(random.nextInt(remainingShorts.size()));

        remainingShorts.remove(shortStageA);

        StageOption shortStageB = remainingShorts.get(random.nextInt(remainingShorts.size()));
        remainingShorts.remove(shortStageB);

        StageOption shortStageC = remainingShorts.get(random.nextInt(remainingShorts.size()));


        return List.of(
                stage()
                        .stageConditionOption(generateStageCondition(countryOption))
                        .degradation(generateSurfaceDegradation())
                        .serviceArea(ServiceArea.LONG)
                        .route(shortStageA),
                stage()
                        .stageConditionOption(generateStageCondition(countryOption))
                        .degradation(generateSurfaceDegradation())
                        .serviceArea(ServiceArea.NONE)
                        .route(shortStageB),
                stage()
                        .stageConditionOption(generateStageCondition(countryOption))
                        .degradation(generateSurfaceDegradation())
                        .serviceArea(ServiceArea.NONE)
                        .route(longStage),
                stage()
                        .stageConditionOption(generateStageCondition(countryOption))
                        .degradation(generateSurfaceDegradation())
                        .serviceArea(ServiceArea.NONE)
                        .route(shortStageC)
        );
    }

    private CountryOption generateCountry(long clubId) {
        final var previouslyGeneratedEvents = getLastGeneratedEvents(clubId);

        if (requiresNewCountry(previouslyGeneratedEvents)) {
            List<CountryOption> previousCountryOptions = previouslyGeneratedEvents.stream()
                    .map(Event::getCountry).map(CountryOption::findById).toList();
            return cycleOptionsSelector.generate(Arrays.asList(CountryOption.values()), previousCountryOptions);
        } else {
            Event lastEvent = previouslyGeneratedEvents.get(0);
            return CountryOption.findById(lastEvent.getCountry());
        }
    }

    private VehicleClass generateVehicle() {
        return VehicleClass.CC_2000;
    }

    private boolean requiresNewCountry(List<Event> previouslyGeneratedEvents) {
        log.info("Checking if new country is required, previous: {}", previouslyGeneratedEvents.stream().map(Event::getCountry).collect(Collectors.joining(",")));
        if (previouslyGeneratedEvents.size() == 0) {
            log.info("No previous events, new country required");
            return true;
        } else {
            final var lastGeneratedCountries = previouslyGeneratedEvents.stream().limit(COUNTRY_CYCLE_LENGTH).map(Event::getCountry).toList();

            if (lastGeneratedCountries.size() < COUNTRY_CYCLE_LENGTH) {
                log.info("No 4 events yet, new country required");
                return false;
            } else {
                log.info("Check 4 events uniqueness {}",Set.copyOf(lastGeneratedCountries).size() == 1);
                return Set.copyOf(lastGeneratedCountries).size() == 1;
            }
        }
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

    public List<Event> getLastGeneratedEvents(long clubId) {
        List<Event> events = clubRepository.findByReferenceId(clubId).map(club -> {
            var previouslyGeneratedEvents = club.getChampionships().stream()
                    .flatMap(championship -> championship.getEvents().stream())
                    .collect(Collectors.toList());
            Collections.reverse(previouslyGeneratedEvents);

            return previouslyGeneratedEvents;
        }).orElse(List.of());

        List<List<Event>> partition = Lists.partition(events, 13);

        if(partition.size() == 0) {
            return List.of();
        }

        List<Event> lastEvents = partition.get(partition.size() - 1);

        if(lastEvents.size() == 13) {
            return List.of();
        } else {
            return lastEvents;
        }
    }


}
