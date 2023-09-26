package io.busata.fourleft.domain.dirtrally2;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WeightedOccurenceSelector {

    public <T extends Enum<T>> T generate(List<T> uniqueOptions, List<T> occurrences, int limit) {
        final var countryCounts = occurrences.stream().limit(limit)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        final var weightedCountryOccurence = uniqueOptions.stream().map(option ->
                Pair.create(option, generateWeight(countryCounts, option))).collect(Collectors.toList());

        log.info("Chances:");
        EnumeratedDistribution<T> enumeratedDistribution = new EnumeratedDistribution<>(weightedCountryOccurence);
        enumeratedDistribution.getPmf().forEach(pair -> {
            log.info("{}: {}%", pair.getKey(), String.format("%,.5f", pair.getValue()));
        });

        return enumeratedDistribution.sample();
    }

    private <T> double generateWeight(Map<T, Long> countryCounts, T option) {
        int occurrences = countryCounts.getOrDefault(option, 0L).intValue();

        return occurrences == 0 ? 1 : 0.01;
    }
}
