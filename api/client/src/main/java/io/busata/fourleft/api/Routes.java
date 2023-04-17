package io.busata.fourleft.api;

public class Routes {
    /*
        Club routes
     */
    public static final String CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID = "/api/views/{viewId}/results/current";
    public static final String CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID = "/api/views/{viewId}/results/previous";

    public static final String CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID = "/api/views/{viewId}/standings/current";
    public static final String CLUB_VIEWS_PREVIOUS_STANDINGS_BY_VIEW_ID = "/api/views/{viewId}/standings/previous";

    public static final String RESULT_VIEW_EVENT_RESTRICTION = "/api/views/{resultViewId}/restrictions";
    public static final String CLUB_VIEWS_EVENT_SUMMARY_BY_VIEW_ID = "/api/views/{viewId}/event_summary";
    public static final String CLUB_VIEWS_REFRESH = "/api/views/{viewId}/refresh";

    public static final String CLUB_MEMBERS_BY_VIEW_ID = "/api/clubs/{viewId}/members";
    public static final String CLUB_TIERS_BY_CLUB_ID = "/api/clubs/{clubId}/tiers";
    public static final String TIER_RESULTS_BY_CLUB_ID = "/api/clubs/{clubId}/tiers/results/current";

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

    public static final String QUERY_VEHICLE_CLASS = "/api/query/vehicle_class/{vehicleClass}";

    /*
        Field Mappings
     */

    public static final String FIELD_MAPPINGS = "/api/discord/field_mappings";
    public static final String FIELD_MAPPING_BY_ID = "/api/discord/field_mappings/{id}";

    /*
        Message management
     */

    public static final String DISCORD_ALL_MESSAGES = "/api/discord/messages";
    public static final String DISCORD_MESSAGE_DETAILS = "/api/discord/messages/{messageId}";
    public static final String DISCORD_MESSAGE = "/api/discord/message";
    public static final String MESSAGE_EVENTS = "/api/events/messages";
    public static final String MESSAGE_EVENT_BY_EVENT_ID = "/api/events/messages/{eventId}";

    public static final String MESSAGE_BY_CHANNEL_ID = "/api/messages/{channelId}";
    public static final String MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID = "/api/messages/{channelId}/{messageId}";

    /*
        Tiers
     */
    public static final String TIER_RESULTS_BY_TIER_ID = "/api/tier/{tierId}/results/current";

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
    public static final String USER_COMMUNITY_PROGRESSION = "/api/public/users/community/progression";

    public static final String ALL_DISCORD_CHANNEL_CONFIGURATION = "/api/discord/configurations";
    public static final String DISCORD_CHANNEL_CONFIGURATION = "/api/discord/channels/{channelId}/configuration";
    public static final String DISCORD_CALLBACK = "/api/public/discord/callback";
    public static final String DISCORD_INTEGRATION_AUTH = "/api/discord/integration/auth";

    public static final String DISCORD_GUILDS = "/api/discord/integration/guilds";
    public static final String DISCORD_AUTHENTICATION_STATUS = "/api/discord/integration/authentication_status";
    public static final String DISCORD_REDIRECT = "/api/public/discord/redirect";
    public static final String DISCORD_INVITE_BOT = "/api/public/discord/invite_bot";

    public static final String DISCORD_MANAGE_SERVER = "/api/discord/integration/guilds/{guildId}/can_manage";
    public static final String DISCORD_GUILD = "/api/discord/integration/guilds/{guildId}";

    public static final String DISCORD_GUILD_CHANNELS = "/api/discord/integration/guilds/{guildId}/channels";

    public static final String SECURITY_USER = "/api/security/user";
}
