package io.busata.fourleft.api;

import io.busata.fourleft.api.models.*;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface FourLeftApi {

    @GetMapping(RoutesTo.CLUB_MEMBERS_BY_VIEW_ID)
    List<ClubMemberTo> getMembers(@PathVariable UUID viewId);

    @PostMapping(RoutesTo.COMMUNITY_TRACK_USER)
    void trackUser(@RequestBody TrackUserRequestTo request);

    @GetMapping(RoutesTo.COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getCommunityResults();

    @PostMapping(RoutesTo.REQUEST_ALIAS_UPDATE)
    AliasRequestResultTo requestAliasUpdate(@RequestBody AliasUpdateRequestTo aliasUpdateRequestTo);

    @GetMapping(RoutesTo.CLUB_VIEWS_EVENT_SUMMARY_BY_VIEW_ID)
    ViewEventSummaryTo getViewEventSummary(@PathVariable UUID viewId);

    @GetMapping(RoutesTo.PREVIOUS_COMMUNITY_RESULTS)
    List<CommunityChallengeSummaryTo> getCommunityResultsFromYesterday();

    @GetMapping(RoutesTo.FIELD_MAPPINGS)
    List<FieldMappingTo> getFieldMappings();

    @PostMapping(RoutesTo.FIELD_MAPPINGS)
    FieldMappingTo createFieldMapping(@RequestBody FieldMappingRequestTo request);

    @GetMapping(RoutesTo.QUERY_TRACK)
    QueryTrackResultsTo queryTrack(@RequestParam String stageName);
    @GetMapping(RoutesTo.QUERY_NAME)
    List<String> queryUsername(@RequestParam String query);

    @PostMapping(RoutesTo.DISCORD_ALL_MESSAGES)
    void postMessage(@RequestBody MessageLogTo messageLog);

    @GetMapping(RoutesTo.DISCORD_MESSAGE_DETAILS)
    MessageLogTo getMessageDetails(@RequestParam long messageId);

    @GetMapping(RoutesTo.DISCORD_MESSAGE)
    boolean hasMessage(@RequestParam long messageId, @RequestParam MessageType messageType);

    @GetMapping(RoutesTo.USER_OVERVIEW)
    UserResultSummaryTo getUserResultSummary(@RequestParam String query);

    @GetMapping(RoutesTo.CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID)
    ViewResultTo getViewCurrentResults(@PathVariable UUID viewId);
    @GetMapping(RoutesTo.CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID)
    ViewResultTo getViewPreviousResults(@PathVariable UUID viewId);
    @GetMapping(RoutesTo.CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID)
    ViewPointsTo getViewCurrentStanding(@PathVariable UUID viewId);
    @GetMapping(RoutesTo.CLUB_VIEWS_PREVIOUS_STANDINGS_BY_VIEW_ID)
    ViewPointsTo getViewPreviousStandings(@PathVariable UUID viewId);
}
