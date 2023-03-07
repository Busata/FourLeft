package io.busata.fourleft.club.results.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ClubSummaryFactorytoTest {

    @Test
    public void testSortingStages()  {

        List<String> strings = List.of("1", "5", "3", "2","4", "8","0", "7", "6").stream().sorted().toList();

        assertThat(strings).containsExactly("0","1","2","3","4","5","6","7","8");
    }

    @Test
    public void testFlatMapOptional() {

        String value = Optional.of("5").flatMap((a) -> Optional.<String>empty()).orElse("haha");

        assertThat(value).isEqualTo("haha");

    }

}