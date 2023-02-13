package io.busata.fourleft.api;

public class Routes {
    /*
        Club routes
     */
    public static final String CLUB_RESULTS_CURRENT_EXPORT = "/api/public/clubs/{clubId}/current/export";
    public static final String CLUB_RESULTS_PREVIOUS_EXPORT = "/api/public/clubs/{clubId}/previous/export";
    public static final String CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID = "/api/views/{viewId}/results/current";
    public static final String CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID = "/api/views/{viewId}/results/previous";

    public static final String CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID = "/api/views/{viewId}/standings/current";
    public static final String CLUB_VIEWS_PREVIOUS_STANDINGS_BY_VIEW_ID = "/api/views/{viewId}/standings/previous";

    public static final String CLUB_VIEWS_EVENT_SUMMARY_BY_VIEW_ID = "/api/views/{viewId}/event_summary";

    public static final String CLUB_STANDINGS_BY_CLUB_ID = "/api/clubs/{clubId}/championship_standings";
    public static final String CLUB_CUSTOM_STANDINGS_BY_CLUB_ID = "/api/clubs/{clubId}/custom_standings";
    public static final String CLUB_MEMBERS_BY_CLUB_ID = "/api/clubs/{clubId}/members";
    public static final String CLUB_EVENT_SUMMARY_BY_CLUB_ID = "/api/clubs/{clubId}/event_summary";
    public static final String CLUB_TIERS_BY_CLUB_ID = "/api/clubs/{clubId}/tiers";
    public static final String CLUB_ACTIVE_EVENT_BY_CLUB_ID = "/api/clubs/{clubId}/active_event";
    public static final String TIER_RESULTS_BY_CLUB_ID = "/api/clubs/{clubId}/tiers/results/current";
    public static final String REFRESH_CLUB_BY_CLUB_ID = "/api/clubs/{clubId}/refresh";
    public static final String STAGE_SUMMARY_BY_CLUB_ID = "/api/clubs/{clubId}/stage_summary";

    /*
        Community
     */

    public static final String COMMUNITY_TRACK_USER = "/api/community/track_user";
    public static final String COMMUNITY_RESULTS = "/api/community/results";
    public static final String PREVIOUS_COMMUNITY_RESULTS = "/api/community/results/yesterday";
    public static final String GET_TRACKED_USERS = "/api/community/users";
    public static final String GET_TRACKED_USER_BY_ID = "/api/community/users/{id}";

    /*
        Query
     */
    public static final String QUERY_TRACK = "/api/query/track";
    public static final String QUERY_NAME = "/api/query/name";

    /*
        Field Mappings
     */

    public static final String FIELD_MAPPINGS = "/api/discord/field_mappings";
    public static final String FIELD_MAPPING_BY_ID = "/api/discord/field_mappings/{id}";

    /*
        Message management
     */

    public static final String DISCORD_ALL_MESSAGES = "/api/discord/messages";
    public static final String DISCORD_MESSAGE = "/api/discord/message";
    public static final String MESSAGE_EVENTS = "/api/events/messages";
    public static final String MESSAGE_EVENT_BY_EVENT_ID = "/api/events/messages/{eventId}";

    public static final String MESSAGE_BY_CHANNEL_ID = "/api/messages/{channelId}";
    public static final String MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID = "/api/messages/{channelId}/{messageId}";

    /*
        Tiers
     */
    public static final String TIER_RESULTS_BY_TIER_ID = "/api/tier/{tierId}/results/current";
    public static final String PLAYERS = "/api/players";
    public static final String PLAYER_BY_PLAYER_ID = "/api/players/{playerId}";
    public static final String PLAYER_BY_TIER_ID = "/api/players/by_tier/{tierId}";
    public static final String TIER_PLAYER_BY_TIER_ID_AND_PLAYER_ID = "/api/tiers/{tierId}/player/{playerId}";
    public static final String CLEAR_PLAYER_TIERS_BY_PLAYER_ID = "/api/players/{playerId}/clear_tiers";
    public static final String TIER_VEHICLE_RESTRICTIONS_BY_TIER_ID_AND_CHALLENGE_ID_AND_EVENT_ID = "/api/tiers/{tierId}/competition/{challengeId}/{eventId}/vehicles";

    /*
        Automated creation
     */
    public static final String TEST_DAILY_CREATION_BY_CLUB_ID = "/api/championships_creation/{clubId}/test_daily";
    public static final String TEST_MONTHLY_CREATION_BY_CLUB_ID = "/api/championships_creation/{clubId}/test_monthly";

    public static final String COPY_CHAMPIONSHIP_TO_CLUB = "/api/public/copy_event/{fromClubId}";

    /*
        Misc
     */
    public static final String MERGE_CLUB = "/api/merge";
    public static final String USER_OVERVIEW = "/api/users/overview";
    public static final String DISCORD_CHANNEL_CONFIGURATION = "/api/discord/channel_configurations";
}
