package io.busata.fourleftdiscord.helpers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class ListHelpersTest {

    @Test
    public void createMultipleGroups() {

        final var testData = IntStream.range(0, 30).boxed().toList();

        List<List<Integer>> lists = ListHelpers.partitionInGroups(testData, 12, 10);

        assertEquals(3, lists.size());
        assertEquals(12, lists.get(0).size());
        assertEquals(10, lists.get(1).size());
        assertEquals(8, lists.get(2).size());
    }

    @Test
    public void smallerThanInitialList() {

        final var testData = IntStream.range(0, 10).boxed().toList();

        List<List<Integer>> lists = ListHelpers.partitionInGroups(testData, 12, 10);

        assertEquals(1, lists.size());
        assertEquals(10, lists.get(0).size());
    }

    @Test
    public void initialListOnly() {

        final var testData = IntStream.range(0, 12).boxed().toList();

        List<List<Integer>> lists = ListHelpers.partitionInGroups(testData, 12, 10);

        assertEquals(1, lists.size());
        assertEquals(12, lists.get(0).size());
    }

    @Test
    public void emptyList() {

        List<List<Integer>> lists = ListHelpers.partitionInGroups(List.of(), 12, 10);

        assertEquals(0, lists.size());
    }
}