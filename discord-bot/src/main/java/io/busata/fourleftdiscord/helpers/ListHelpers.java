package io.busata.fourleftdiscord.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ListHelpers {
    public static <U> List<List<U>> partitionInGroups(List<U> sortedEntries, int... groupSizes) {
        if(sortedEntries.size() == 0) {
            return List.of();
        }

        return sortedEntries.stream().reduce(new ArrayList<>(), (lists, pair) -> {
            if (lists.size() == 0) {
                lists.add(new ArrayList<>());
            }

            int groupSizeIdx = Math.min(groupSizes.length - 1, lists.size() - 1);
            int groupSize = groupSizes[groupSizeIdx];

            if (firstItemOf(lists).size() < groupSize) {
                firstItemOf(lists).add(pair);
                return lists;
            } else {
                if(lists.size() == 1) {
                    lists.add(new ArrayList<>());
                }
            }

            if (lastItemOf(lists).size() >= groupSize) {
                lists.add(new ArrayList<>());
            }

            lastItemOf(lists).add(pair);

            return lists;

        }, (List<List<U>> a, List<List<U>> b) -> Stream.concat(a.stream(), b.stream()).toList());
    }

    public static <U> U lastItemOf(List<U> items) {
        if(items.size() == 0) {
            throw new IllegalArgumentException("Item has no last item");
        }

        return items.get(items.size() - 1);
    }

    public static <U> U firstItemOf(List<U> items) {
        if(items.size() == 0) {
            throw new IllegalArgumentException("Item has no last item");
        }

        return items.get(0);
    }
}
