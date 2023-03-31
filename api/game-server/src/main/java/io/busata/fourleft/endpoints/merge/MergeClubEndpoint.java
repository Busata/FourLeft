package io.busata.fourleft.endpoints.merge;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.MergeRequestTo;
import io.busata.fourleft.api.models.MergedBoardEntry;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Leaderboard;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.importer.updaters.LeaderboardFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MergeClubEndpoint {

    private final LeaderboardFetcher leaderboardFetcher;
    private final LeaderboardRepository leaderboardRepository;

    @PostMapping(Routes.MERGE_CLUB)
    public List<MergedBoardEntry> mergeClubs(@RequestBody MergeRequestTo request) {
        log.info("Getting leaderboard for first club");
        leaderboardFetcher.upsertBoard(request.firstClub());
        log.info("Getting leaderboard for second club");
        leaderboardFetcher.upsertBoard(request.secondClub());

        Leaderboard firstLeaderboard = leaderboardRepository.findLeaderboard(request.firstClub()).orElseThrow();

        Leaderboard secondLeaderboard = leaderboardRepository.findLeaderboard(request.secondClub()).orElseThrow();

        HashMap<String, List<BoardEntry>> mergedEntries = new HashMap<>();

        firstLeaderboard.getEntries().forEach(entry -> {
            List<BoardEntry> entryList = mergedEntries.computeIfAbsent(entry.getName(), k -> new ArrayList<>());
            entryList.add(entry);
        });
        secondLeaderboard.getEntries().forEach(entry -> {
            List<BoardEntry> entryList = mergedEntries.computeIfAbsent(entry.getName(), k -> new ArrayList<>());
            entryList.add(entry);
        });

        return mergedEntries.entrySet().stream().map(entrySet -> {

            List<Duration> durations = new ArrayList<>();
            if(entrySet.getValue().size() > 0) {
                durations.add(parseRallyTime(entrySet.getValue().get(0).getTotalTime()));
            }
            if(entrySet.getValue().size() > 1) {
                durations.add(parseRallyTime(entrySet.getValue().get(1).getTotalTime()));
            }

            final var mergedDuration = durations.size() == 1 ? durations.get(0) : durations.get(0).plus(durations.get(1));


            return new MergedBoardEntry(entrySet.getKey(), mergedDuration, durations.size(),  "%s:%s:%s.%s".formatted(
                    mergedDuration.toHoursPart(),
                    mergedDuration.toMinutesPart(),
                    mergedDuration.toSecondsPart(),
                    mergedDuration.toNanosPart()
                    ));
        }).filter(mergedBoardEntry -> mergedBoardEntry.entriesDone() == 2).sorted(Comparator.comparing(MergedBoardEntry::duration)).collect(Collectors.toList());

    }

    private Duration parseRallyTime(String rallyTime) {
        String[] splitted = rallyTime.split(":");
        if(splitted.length == 3) {
            //hours minutes seconds
            int hours = Integer.parseInt(splitted[0]);
            int minutes = Integer.parseInt(splitted[1]);

            String[] secondSplit = splitted[2].split("\\.");

            int seconds = Integer.parseInt(secondSplit[0]);
            int nanoseconds = Integer.parseInt(secondSplit[1]);

            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusNanos(nanoseconds);
        } else {
            //minutes seconds

            int minutes = Integer.parseInt(splitted[0]);

            String[] secondSplit = splitted[1].split("\\.");

            int seconds = Integer.parseInt(secondSplit[0]);
            int nanoseconds = Integer.parseInt(secondSplit[1]);

            return Duration.ofMinutes(minutes).plusSeconds(seconds).plusNanos(nanoseconds);

        }
    }
}
