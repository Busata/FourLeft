package io.busata.fourleft.endpoints.club.automated.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class CycleOptionsSelector {
    private Random random = new Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));

    public <T extends Enum<T>> T generate(List<T> uniqueOptions, List<T> occurrences) {
        final var limit = uniqueOptions.size();

        final var remainder = occurrences.size() % limit;

        final var lastBag = occurrences.stream().limit(remainder).toList();

        final var possibleOptions = ListUtils.subtract(uniqueOptions, lastBag);

        return possibleOptions.get(random.nextInt(possibleOptions.size()));
    }
}
