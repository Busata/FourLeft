package io.busata.fourleft.application.dirtrally2;

import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleft.api.models.StageOptionTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.domain.dirtrally2.clubs.UniquePlayersViewRepository;
import io.busata.fourleft.domain.dirtrally2.options.CountryConfiguration;
import io.busata.fourleft.domain.dirtrally2.options.CountryOption;
import io.busata.fourleft.domain.dirtrally2.options.StageConfiguration;
import io.busata.fourleft.domain.dirtrally2.options.StageOption;
import io.busata.fourleft.domain.dirtrally2.options.VehicleClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {
    private final UniquePlayersViewRepository uniquePlayersViewRepository;

    public List<VehicleTo> getVehiclesForClass(@PathVariable String vehicleClass) {
        VehicleClass byId = VehicleClass.findById(vehicleClass);
        return byId.getVehicles().stream().map(vehicle -> new VehicleTo(vehicle.name(), vehicle.getDisplayName())).toList();
    }


    public List<String> queryName(String query) {
        log.info("Finding name {}", query);
        return uniquePlayersViewRepository.findSimilar(query);
    }

    public QueryTrackResultsTo queryTrack(@RequestParam String stageName) {

        int closestRatio = Integer.MIN_VALUE;
        StageOption closestOption = null;

        String normalized = Normalizer.normalize(stageName, Normalizer.Form.NFD).toLowerCase();

        for (StageOption value : StageOption.values()) {
            String normalizedValue = Normalizer.normalize(value.getDisplayName(), Normalizer.Form.NFD).toLowerCase();
            int ratio = FuzzySearch.ratio(normalized, normalizedValue);

            log.info("{} vs {} = {}",normalized, normalizedValue, ratio);

            if (ratio > closestRatio) {
                closestRatio = ratio;
                closestOption = value;
            }
        }

        log.info("User input {}, he probably meant: {}. (Distance: {}) ", stageName, closestOption, closestRatio);

        CountryOption country = closestOption.country();

        StageConfiguration stageConfiguration = findStageConfiguration(closestOption);
        StageConfiguration reverseStageConfiguration = findReverseConfiguration(stageConfiguration);

        return new QueryTrackResultsTo(
                country.getId(),
                country.displayName,
                new StageOptionTo(
                        stageConfiguration.longStage().getDisplayName(),
                        stageConfiguration.longStage().getLengthKm(),
                        stageConfiguration.longStage() == closestOption
                ),
                new StageOptionTo(
                        stageConfiguration.firstShort().getDisplayName(),
                        stageConfiguration.firstShort().getLengthKm(),
                        stageConfiguration.firstShort() == closestOption
                ),
                new StageOptionTo(
                        stageConfiguration.secondShort().getDisplayName(),
                        stageConfiguration.secondShort().getLengthKm(),
                        stageConfiguration.secondShort() == closestOption
                ),
                new StageOptionTo(
                        reverseStageConfiguration.longStage().getDisplayName(),
                        reverseStageConfiguration.longStage().getLengthKm(),
                        reverseStageConfiguration.longStage() == closestOption
                ),
                new StageOptionTo(
                        reverseStageConfiguration.firstShort().getDisplayName(),
                        reverseStageConfiguration.firstShort().getLengthKm(),
                        reverseStageConfiguration.firstShort() == closestOption
                ),
                new StageOptionTo(
                        reverseStageConfiguration.secondShort().getDisplayName(),
                        reverseStageConfiguration.secondShort().getLengthKm(),
                        reverseStageConfiguration.secondShort() == closestOption
                )
        );
    }

    private StageConfiguration findStageConfiguration(StageOption closestOption) {
        for (StageConfiguration value : StageConfiguration.values()) {
            if(value.containsOption(closestOption)) {
                return value;
            }
        }
        throw new RuntimeException("Could not find the stage configuration");
    }

    private StageConfiguration findReverseConfiguration(StageConfiguration closestOption) {
        for (CountryConfiguration value : CountryConfiguration.values()) {
            Optional<StageConfiguration> reverse = value.findReverse(closestOption);
            if(reverse.isPresent()) {
                return reverse.get();
            }
        }
        throw new RuntimeException("Could not find the stage configuration");
    }
}
