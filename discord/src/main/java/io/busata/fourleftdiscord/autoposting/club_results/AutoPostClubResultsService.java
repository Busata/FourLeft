package io.busata.fourleftdiscord.autoposting.club_results;

import discord4j.common.util.Snowflake;
import feign.FeignException;
import io.busata.fourleft.api.events.LeaderboardUpdated;
import io.busata.fourleft.api.events.QueueNames;
import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.autoposting.club_results.domain.AutoPostEntry;
import io.busata.fourleftdiscord.autoposting.club_results.domain.AutoPostEntryRepository;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableFactory;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.client.FourLeftClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static io.busata.fourleftdiscord.autoposting.club_results.AutopostClubResultsMessageService.createAutopostEntry;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoPostClubResultsService {
    private final FourLeftClient api;
    private final AutoPostEntryRepository autoPostEntryRepository;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;
    private final AutopostClubResultsMessageService autopostClubResultsMessageService;
    private final AutoPostableFactory autoPostableFactory;


    @RabbitListener(queues = QueueNames.LEADERBOARD_UPDATE)
    public void updateClub(LeaderboardUpdated event) {
        try {
            log.info("-- Leaderboards for {} were updated", event.clubId());

            discordChannelConfigurationService.getConfigurations()
                    .stream()
                    .filter(DiscordChannelConfigurationTo::enableAutoposts)
                    .filter(configuration -> configuration.includesClub(event.clubId()))
                    .forEach(this::tryPostingResults);

            discordChannelConfigurationService.getConfigurations()
                    .stream()
                    .filter(config -> !config.enableAutoposts())
                    .filter(configuration -> configuration.includesClub(event.clubId()))
                    .forEach(this::saveEntries);
        } catch (Exception ex) {
            log.error("!! !! Something wrong while posting auto results", ex);
        }
    }

    private void tryPostingResults(DiscordChannelConfigurationTo configuration) {
        log.info("-- Checking for channel {}", configuration.clubView().description());
        try {
            tryPostingNewEntries(configuration.clubView(), Snowflake.of(configuration.channelId()));
        }
        catch (FeignException.NotFound ex) {
            log.warn("No results found");
        } catch (Exception ex) {
            log.warn("!! !! Something wrong while posting auto results, probably has no current event", ex);
        }
    }

    private void saveEntries(DiscordChannelConfigurationTo configuration) {
        final var newResults = api.getViewCurrentResults(configuration.clubView().id());
        final var channelId = Snowflake.of(configuration.channelId());

        List<String> unpostedEntries = newResults.getMultiListResults().stream().flatMap(namedListResult -> {
            final var postedEntries = findPostedEntries(channelId.asString() + "#" + newResults.getViewEventKey());

            return findUnposted(namedListResult.results(), postedEntries).stream();
        }).collect(Collectors.toList());

        final var multiView = autoPostableFactory.create(newResults, unpostedEntries);

        multiView.getMultiListResults().stream().flatMap(multiList -> {
            return multiList.results().stream().map(entry -> {
                final var autoPostEntry = new AutoPostEntry();
                createAutopostEntry(autoPostEntry, entry, -1L, channelId.asString() + "#" + multiView.getEventKey());

                return autoPostEntry;
            });
        }).forEach(autoPostEntryRepository::save);

        log.info("Saved {} entries without posting for {}", unpostedEntries.size(), configuration.clubView().description());
    }

    private void tryPostingNewEntries(ClubViewTo clubViewTo, Snowflake channelId) {
        final var newResults = api.getViewCurrentResults(clubViewTo.id());

        List<String> unpostedEntries = newResults.getMultiListResults().stream().flatMap(namedListResult -> {
            final var postedEntries = findPostedEntries(channelId.asString() + "#" + newResults.getViewEventKey());

            return findUnposted(namedListResult.results(), postedEntries).stream();
        }).limit(10).collect(Collectors.toList());

        log.info("-- -- Unposted entries: {}", unpostedEntries.size());

        if(unpostedEntries.isEmpty()) {
            return;
        }

        autopostClubResultsMessageService.postNewEntries(channelId, newResults, unpostedEntries);
    }

    private List<AutoPostEntry> findPostedEntries(String viewEventKey) {
        log.info("Checking posted entries -- {} --", viewEventKey);
        return autoPostEntryRepository.findByEventKey(viewEventKey);
    }

    private List<String> findUnposted(List<DriverEntryTo> entries, List<AutoPostEntry> postedEntries) {
        final var unpostedByName = findUnpostedEntries(entries, postedEntries, (newEntry, postedEntry) -> postedEntry.hasEqualName(newEntry));
        final var unpostedByTimeVehicleAndNationality = findUnpostedEntries(entries , postedEntries, (newEntry, postedEntry) -> postedEntry.hasEqualTimeVehicleAndNationality(newEntry));

        if(unpostedByName.size() == unpostedByTimeVehicleAndNationality.size()) {
            return unpostedByName;
        } else {
            return unpostedByTimeVehicleAndNationality;
        }
    }


    private List<String> findUnpostedEntries(List<DriverEntryTo> entries, List<AutoPostEntry> postedEntries, BiPredicate<DriverEntryTo, AutoPostEntry> entryFilter) {
        return entries.stream()
                .filter(newEntry -> postedEntries.stream().noneMatch(postedEntry -> entryFilter.test(newEntry, postedEntry)))
                .map(DriverEntryTo::racenet)
                .collect(Collectors.toList());

    }

}
