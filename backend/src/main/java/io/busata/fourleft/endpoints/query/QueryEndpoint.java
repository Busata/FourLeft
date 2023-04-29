package io.busata.fourleft.endpoints.query;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleft.api.models.StageOptionTo;
import io.busata.fourleft.api.models.views.VehicleTo;
import io.busata.fourleft.domain.dirtrally2.clubs.repository.UniquePlayersViewRepository;
import io.busata.fourleft.domain.dirtrally2.options.models.CountryConfiguration;
import io.busata.fourleft.domain.dirtrally2.options.models.CountryOption;
import io.busata.fourleft.domain.dirtrally2.options.models.StageConfiguration;
import io.busata.fourleft.domain.dirtrally2.options.models.StageOption;
import io.busata.fourleft.domain.dirtrally2.options.models.VehicleClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.reverseOrder;

@RestController
@RequiredArgsConstructor
@Slf4j
public class QueryEndpoint
{
    private final UniquePlayersViewRepository uniquePlayersViewRepository;

    @GetMapping(Routes.QUERY_VEHICLE_CLASS)
    public List<VehicleTo> getVehiclesForClass(@PathVariable String vehicleClass) {
        VehicleClass byId = VehicleClass.findById(vehicleClass);
        return byId.getVehicles().stream().map(vehicle -> new VehicleTo(vehicle.name(), vehicle.getDisplayName())).toList();
    }

    @GetMapping(Routes.QUERY_NAME)
    public List<String> queryName(@RequestParam String query) {
        log.info("Finding name {}", query);
        return uniquePlayersViewRepository.findSimilar(query);
    }

    private static String normalizeName(String query) {
        return Normalizer.normalize(query.toLowerCase().replace(" ", ""), Normalizer.Form.NFC);
    }

    @GetMapping(Routes.QUERY_TRACK)
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
