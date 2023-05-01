package io.busata.fourleft.application.aggregators.helpers;

import io.busata.fourleft.application.aggregators.FixedPointChampionshipFetcher;
import io.busata.fourleft.domain.dirtrally2.clubs.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.aggregators.points.FixedPointsCalculator;
import io.busata.fourleft.domain.aggregators.points.PointSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FixedPointChampionshipFetcherTest {

    @Test
    public void shouldTakePreviousChampionshipIfCurrentOneHasNoActiveEvent() {
        FixedPointChampionshipFetcher fetcher = new FixedPointChampionshipFetcher();
        FixedPointsCalculator calculator = new FixedPointsCalculator(1, null, new PointSystem());

        List<Championship> championships = List.of(
                Championship.championship().referenceId("1").name("Expected").events(List.of(
                        Event.event().eventStatus("Finished").build()
                )).build(),
                Championship.championship().referenceId("2").name("Ignored").events(List.of(
                                Event.event().eventStatus("Active").build()
                        ))
                        .build()
        );

        List<Championship> filteredChampionships = fetcher.filterChampionships(championships, calculator);

        assertThat(filteredChampionships).extracting(Championship::getName).containsExactly("Expected");
    }

}