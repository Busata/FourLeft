package io.busata.fourleft.backendeasportswrc.application.discord.autoposting;


import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary.MixedEntry;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService;
import io.busata.fourleft.backendeasportswrc.application.discord.results.ChannelResultsService.ClassEvent;
import io.busata.fourleft.backendeasportswrc.application.discord.results.MergedRanking;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutopostEntry;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.DiscordMessageTo;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostEditMessageEvent;
import io.busata.fourleft.backendeasportswrc.domain.events.AutoPostNewMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordAutoPostingService {

    public static final int ENTRIES_LIMIT = 10;

    private final DiscordGateway discordGateway;

    private final AutopostingEntryService autopostingEntryService;

    private final ClubService clubService;

    private final ClubLeaderboardService clubLeaderboardService;

    private final DiscordClubConfigurationService clubConfigurationService;

    private final ChannelResultsService channelResultsService;

    private final ApplicationEventPublisher publisher;

    // A MIXED channel is synced from each of its clubs' import threads; one sync per channel at a time, or
    // two threads could both post the same new entries.
    private final Map<Long, Object> channelLocks = new ConcurrentHashMap<>();

    /**
     * What one sync works on: the board's entries, the rows already posted for it, the posting order and
     * how an entry maps to its event (a MIXED board spans one event per club). Entries are told apart by
     * event + player, as the same driver can enter two classes.
     */
    private record Board(List<ClubLeaderboardEntry> entries,
                         List<AutopostEntry> posted,
                         Comparator<ClubLeaderboardEntry> order,
                         Function<ClubLeaderboardEntry, String> eventIdOf,
                         Function<List<ClubLeaderboardEntry>, AutoPostMessageSummary> summary) {

        String keyOf(ClubLeaderboardEntry entry) {
            return eventIdOf.apply(entry) + "|" + entry.getPlayerKey();
        }

        static String keyOf(AutopostEntry posted) {
            return posted.getEventId() + "|" + posted.getPlayerKey();
        }
    }

    public void syncResults(String clubId) {
        List<DiscordClubConfiguration> configurations = clubConfigurationService.findPostingForClub(clubId);
        configurations.forEach(config -> {
            if (channelResultsService.isMixed(config)) {
                synchronized (channelLocks.computeIfAbsent(config.getChannelId(), id -> new Object())) {
                    mixedBoard(config).ifPresent(board -> sync(config, board));
                }
            } else {
                singleBoard(clubId, config).ifPresent(board -> sync(config, board));
            }
        });
    }

    private Optional<Board> singleBoard(String clubId, DiscordClubConfiguration configuration) {
        return this.clubService.getActiveEvent(clubId).map(event -> {
            List<ClubLeaderboardEntry> entries = clubLeaderboardService.findEntries(event.getLeaderboardId());
            List<AutopostEntry> posted = autopostingEntryService.findPostedEntries(event.getId(), configuration.getChannelId());

            return new Board(entries, posted,
                    Comparator.comparing(ClubLeaderboardEntry::getRank),
                    entry -> event.getId(),
                    toBePosted -> new AutoPostMessageSummary(event, entries.size(), toBePosted));
        });
    }

    /** All clubs' boards for the primary club's active event as one, ranked overall. */
    private Optional<Board> mixedBoard(DiscordClubConfiguration configuration) {
        return this.clubService.getActiveEvent(configuration.getPrimaryClubId()).map(primaryEvent -> {
            List<ClubLeaderboardEntry> entries = new ArrayList<>();
            List<AutopostEntry> posted = new ArrayList<>();
            Map<ClubLeaderboardEntry, ClassEvent> classEvents = new IdentityHashMap<>();

            List<ClassEvent> matched = channelResultsService.matchedClassEvents(configuration, primaryEvent);
            for (ClassEvent classEvent : matched) {
                clubLeaderboardService.findEntries(classEvent.event().getLeaderboardId()).forEach(entry -> {
                    entries.add(entry);
                    classEvents.put(entry, classEvent);
                });
                posted.addAll(autopostingEntryService.findPostedEntries(classEvent.event().getId(), configuration.getChannelId()));
            }

            MergedRanking ranking = MergedRanking.of(entries);
            Map<ClubLeaderboardEntry, MixedEntry> mixed = new IdentityHashMap<>();
            ranking.ranked().forEach(ranked -> mixed.put(ranked.entry(), new MixedEntry(
                    classEvents.get(ranked.entry()).event().getId(),
                    classEvents.get(ranked.entry()).channelClass().tag(),
                    ranked.rank(),
                    ranked.delta())));

            String vehicleClasses = matched.stream().map(classEvent -> classEvent.channelClass().vehicleClass())
                    .filter(Objects::nonNull).distinct().collect(Collectors.joining(" / "));

            return new Board(entries, posted,
                    Comparator.comparing(ranking::rankOf),
                    entry -> classEvents.get(entry).event().getId(),
                    toBePosted -> new AutoPostMessageSummary(primaryEvent, entries.size(), toBePosted, mixed, vehicleClasses));
        });
    }

    private void sync(DiscordClubConfiguration configuration, Board board) {
        if (configuration.isAutopostingDisabled()) {
            // Remembered as seen, so switching autoposting on later doesn't flood the channel with the backlog.
            autopostingEntryService.saveEntries(board.entries().stream()
                    .map(entry -> new AutopostEntry(board.eventIdOf().apply(entry), configuration.getChannelId(), -1L, entry.getPlayerKey()))
                    .toList());
            return;
        }

        if (board.entries().size() == board.posted().size()) {
            return;
        }

        tryReusingMessage(configuration, board.posted()).ifPresentOrElse(message -> {
            editMessage(configuration, message.id(), board);
        }, () -> {
            createNewMessage(configuration, board);
        });
    }

    private List<ClubLeaderboardEntry> findToBePosted(Long messageId, Board board, boolean requiresTracking) {
        Set<String> postedKeys = board.posted().stream().map(Board::keyOf).collect(Collectors.toSet());

        List<ClubLeaderboardEntry> unpostedEntries = board.entries().stream().filter(entry -> !requiresTracking || entry.isTracked()).filter(newEntry -> !postedKeys.contains(board.keyOf(newEntry))).toList();

        List<AutopostEntry> postedLastTime = board.posted().stream().filter(postedEntry -> postedEntry.getMessageId().equals(messageId)).toList();
        List<ClubLeaderboardEntry> toBeRepostedEntries = postedLastTime.stream().map(postedEntry -> {
           return board.entries().stream().filter(newEntry -> board.keyOf(newEntry).equals(Board.keyOf(postedEntry))).findFirst().orElseThrow();

        }).collect(Collectors.toList());


        int repostLimit = ENTRIES_LIMIT - toBeRepostedEntries.size();

       return Stream.concat(unpostedEntries.stream().sorted(board.order()).limit(repostLimit), toBeRepostedEntries.stream())
                .sorted(board.order())
                .toList();
    }


    private Optional<DiscordMessageTo> tryReusingMessage(DiscordClubConfiguration configuration, List<AutopostEntry> postedEntries) {
        return discordGateway.getLastChannelMessage(configuration.getChannelId()).filter(message -> {
            long postedEntriesCount = postedEntries.stream().filter(entry -> entry.getMessageId().equals(message.id())).count();
            return postedEntriesCount > 0 && postedEntriesCount < ENTRIES_LIMIT;
        });
    }

    private void createNewMessage(DiscordClubConfiguration configuration, Board board) {
        Set<String> postedKeys = board.posted().stream().map(Board::keyOf).collect(Collectors.toSet());
        List<ClubLeaderboardEntry> toBePosted = board.entries().stream().filter(entry -> !configuration.isRequiresTracking() || entry.isTracked()).filter(newEntry -> !postedKeys.contains(board.keyOf(newEntry))).limit(ENTRIES_LIMIT).sorted(board.order()).toList();

        publisher.publishEvent(new AutoPostNewMessageEvent(configuration.getChannelId(), board.summary().apply(toBePosted)));
    }

    private void editMessage(DiscordClubConfiguration configuration, Long messageId, Board board) {
        List<ClubLeaderboardEntry> toBePosted = findToBePosted(messageId, board, configuration.isRequiresTracking());
        publisher.publishEvent(new AutoPostEditMessageEvent(configuration.getChannelId(), messageId, board.summary().apply(toBePosted)));

    }
}
