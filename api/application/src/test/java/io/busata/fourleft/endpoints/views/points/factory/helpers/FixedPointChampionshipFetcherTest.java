package io.busata.fourleft.endpoints.views.points.factory.helpers;

import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointSystem;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateBuilder.championship;
import static io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateEventBuilder.event;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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