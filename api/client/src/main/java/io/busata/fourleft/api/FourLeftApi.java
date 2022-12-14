package io.busata.fourleft.api;

import io.busata.fourleft.api.models.ChampionshipEventSummaryTo;
import io.busata.fourleft.api.models.ChampionshipStandingEntryTo;
import io.busata.fourleft.api.models.ClubMemberTo;
import io.busata.fourleft.api.models.CommunityChallengeSummaryTo;
import io.busata.fourleft.api.models.FieldMappingRequestTo;
import io.busata.fourleft.api.models.FieldMappingTo;
import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleft.api.models.messages.MessageEvent;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.discord.models.MessageType;
import io.busata.fourleft.api.models.QueryTrackResultsTo;
import io.busata.fourleft.api.models.TrackUserRequestTo;
import io.busata.fourleft.api.models.tiers.TierResultTo;
import io.busata.fourleft.api.models.tiers.TierTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface FourLeftApi {



    @GetMapping(value=Routes.CLUB_STANDINGS_BY_CLUB_ID)
    List<ChampionshipStandingEntryTo> getStandings(@PathVariable long clubId);

    @GetMapping(value=Routes.CLUB_CUSTOM_STANDINGS_BY_CLUB_ID)
    List<ChampionshipStandingEntryTo> getCustomStandings(@PathVariable long clubId, @RequestParam int lastXChampionships, @RequestParam boolean powerStageSystem);


    @GetMapping(Routes.CLUB_MEMBERS_BY_CLUB_ID)
    List<ClubMemberTo> getMembers(@PathVariable UUID clubId);

    @PostMapping(Routes.COMMUNITY_TRACK_USER)
    void trackUser(@RequestBody TrackUserRequestTo request);

    @GetMapping(Routes.COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getCommunityResults();

    @GetMapping(Routes.CLUB_EVENT_SUMMARY_BY_CLUB_ID)
    ChampionshipEventSummaryTo getEventSummary(@PathVariable UUID clubId);

    @GetMapping(Routes.PREVIOUS_COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getCommunityResultsFromYesterday();

    @GetMapping(Routes.FIELD_MAPPINGS)
    List<FieldMappingTo> getFieldMappings();

    @PostMapping(Routes.FIELD_MAPPINGS)
    FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request);

    @GetMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    List<ClubViewTo> getConfiguration();

    @GetMapping(Routes.QUERY_TRACK)
    QueryTrackResultsTo queryTrack(@RequestParam String stageName);
    @GetMapping(Routes.QUERY_NAME)
    List<String> queryUsername(@RequestParam String query);

    @PostMapping(Routes.DISCORD_ALL_MESSAGES)
    void postMessage(@RequestBody MessageLogTo messageLog);

    @GetMapping(Routes.DISCORD_MESSAGE)
    boolean hasMessage(@RequestParam long messageId, @RequestParam MessageType messageType);

    @GetMapping(Routes.MESSAGE_EVENTS)
    List<MessageEvent> getEvents();
    @PostMapping(Routes.MESSAGE_EVENT_BY_EVENT_ID)
    void completeEvent(@PathVariable UUID eventId);
    @GetMapping(Routes.CLUB_TIERS_BY_CLUB_ID)
    List<TierTo> getTiers(@PathVariable long clubId);

    @GetMapping(Routes.TIER_RESULTS_BY_TIER_ID)
    TierResultTo getTierResults(@PathVariable String tierId);

    @GetMapping(Routes.TIER_RESULTS_BY_CLUB_ID)
    TierResultTo getAllTierResults(@PathVariable Long clubId);

    @GetMapping(Routes.USER_OVERVIEW)
    UserResultSummaryTo getUserResultSummary(@RequestParam String query);

    @GetMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    List<DiscordChannelConfigurationTo> getDiscordChannelConfigurations();

    @GetMapping(Routes.CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID)
    ViewResultTo getViewCurrentResults(@PathVariable UUID viewId);
    @GetMapping(Routes.CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID)
    ViewResultTo getViewPreviousResults(@PathVariable UUID viewId);
    @GetMapping(Routes.CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID)
    ViewPointsTo getViewCurrentStanding(@PathVariable UUID viewId);
    @GetMapping(Routes.CLUB_VIEWS_PREVIOUS_STANDINGS_BY_VIEW_ID)
    ViewPointsTo getViewPreviousStandings(@PathVariable UUID viewId);
}
