package io.busata.fourleft.endpoints.views.points.factory.helpers;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FixedPointChampionshipFetcherTest {


    @Test
    public void testSortingAndDropWhile() {
        List<String> collect = Stream.of("E", "D", "C", "B")
                .sorted() //It seems wrong to indicate it this way
                .dropWhile(x -> !x.equals("D"))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());


        assertThat(collect).containsExactly("E","D");
    }

}