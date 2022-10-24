package io.busata.fourleftdiscord.autoposting.club_results;

import discord4j.common.util.Snowflake;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.autoposting.club_results.domain.AutoPostEntry;
import io.busata.fourleftdiscord.autoposting.club_results.domain.AutoPostEntryRepository;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.client.FourLeftClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPostClubResultsService {
    private final FourLeftClient api;
    private final AutoPostEntryRepository autoPostEntryRepository;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;
    private final AutopostClubResultsMessageService autopostClubResultsMessageService;

    public void update() {
        discordChannelConfigurationService.getConfigurations()
                .stream()
                .filter(DiscordChannelConfigurationTo::hasAutopostViews)
                .forEach(this::tryPostingResults);
    }

    private void tryPostingResults(DiscordChannelConfigurationTo configuration) {
        log.info("-- Checking for channel {}", configuration.description());
        try {
            configuration.autopostClubViews().forEach(clubViewTo -> {
                tryPostingNewEntries(clubViewTo, Snowflake.of(configuration.channelId()));
            });

        } catch (Exception ex) {
            log.warn("!! !! Something wrong while posting auto results, probably has no current event", ex);
        }
    }

    private void tryPostingNewEntries(ClubViewTo clubViewTo, Snowflake channelId) {
        final var newResults = api.getViewCurrentResults(clubViewTo.id());

        List<String> unpostedEntries = newResults.getMultiListResults().stream().flatMap(namedListResult -> {
            final var eventInfo = namedListResult.eventInfoTo();
            final var postedEntries = autoPostEntryRepository.findByEventIdAndChallengeId(eventInfo.eventId(), eventInfo.eventChallengeId());

            return findUnposted(namedListResult.results(), postedEntries).stream();
        }).limit(10).collect(Collectors.toList());

        if(unpostedEntries.size() == 0) {
            log.debug("Nothing new to post");
            return;
        }

        autopostClubResultsMessageService.postNewEntries(channelId, newResults, unpostedEntries);

    }

    private List<String> findUnposted(List<ResultEntryTo> entries, List<AutoPostEntry> postedEntries) {
        final var unpostedByName = findUnpostedEntries(entries, postedEntries, (newEntry, postedEntry) -> postedEntry.hasEqualName(newEntry));
        final var unpostedByTimeVehicleAndNationality = findUnpostedEntries(entries , postedEntries, (newEntry, postedEntry) -> postedEntry.hasEqualTimeVehicleAndNationality(newEntry));

        if(unpostedByName.size() == unpostedByTimeVehicleAndNationality.size()) {
            return unpostedByName;
        } else {
            return unpostedByTimeVehicleAndNationality;
        }
    }


    private List<String> findUnpostedEntries(List<ResultEntryTo> entries, List<AutoPostEntry> postedEntries, BiPredicate<ResultEntryTo, AutoPostEntry> entryFilter) {
        return entries.stream()
                .filter(newEntry -> postedEntries.stream().noneMatch(postedEntry -> entryFilter.test(newEntry, postedEntry)))
                .map(ResultEntryTo::name)
                .collect(Collectors.toList());

    }

}
