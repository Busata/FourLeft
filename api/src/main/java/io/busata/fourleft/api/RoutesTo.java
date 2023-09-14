package io.busata.fourleft.api;

public class RoutesTo {
    /*
        Club routes
     */
    public static final String ALL_CLUB_VIEWS = "/api/external/views";
    public static final String CLUB_VIEWS_CURRENT_RESULTS_BY_VIEW_ID = "/api/external/views/{viewId}/results/current";
    public static final String CLUB_VIEWS_PREVIOUS_RESULTS_BY_VIEW_ID = "/api/external/views/{viewId}/results/previous";

    public static final String CLUB_VIEWS_CURRENT_STANDINGS_BY_VIEW_ID = "/api/external/views/{viewId}/standings/current";
    public static final String CLUB_VIEWS_PREVIOUS_STANDINGS_BY_VIEW_ID = "/api/external/views/{viewId}/standings/previous";

    public static final String QUERY_NAME = "/api/external/query/name";

    public static final String RESULT_VIEW_EVENT_RESTRICTION = "/api/internal/views/{resultViewId}/restrictions";
    public static final String CLUB_VIEWS_EVENT_SUMMARY_BY_VIEW_ID = "/api/internal/views/{viewId}/event_summary";

    public static final String CLUB_MEMBERS_BY_VIEW_ID = "/api/internal/clubs/{viewId}/members";
    public static final String CLUB_TIERS_BY_CLUB_ID = "/api/internal/clubs/{clubId}/tiers";
    public static final String TIER_RESULTS_BY_CLUB_ID = "/api/internal/clubs/{clubId}/tiers/results/current";

    /*
        Community
     */

    public static final String COMMUNITY_TRACK_USER = "/api/internal/community/track_user";
    public static final String COMMUNITY_RESULTS = "/api/internal/community/results";
    public static final String PREVIOUS_COMMUNITY_RESULTS = "/api/internal/community/results/yesterday";
    public static final String GET_TRACKED_USERS = "/api/internal/community/users";
    public static final String GET_TRACKED_USER_BY_ID = "/api/internal/community/users/{id}";

    /*
        Query
     */
    public static final String QUERY_TRACK = "/api/internal/query/track";

    public static final String QUERY_VEHICLE_CLASS = "/api/internal/query/vehicle_class/{vehicleClass}";


    public static final String REQUEST_ALIAS_UPDATE = "/api/internal/aliases/request";
    public static final String REQUEST_ALIAS_GET= "/api/external/aliases/{requestId}";

    /*
        Field Mappings
     */

    public static final String FIELD_MAPPINGS = "/api/external/discord/field_mappings";
    public static final String FIELD_MAPPING_BY_ID = "/api/internal/discord/field_mappings/{id}";

    /*
        Message management
     */

    public static final String DISCORD_ALL_MESSAGES = "/api/internal/discord/messages";
    public static final String DISCORD_MESSAGE_DETAILS = "/api/internal/discord/messages/{messageId}";
    public static final String DISCORD_MESSAGE = "/api/internal/discord/message";
    public static final String MESSAGE_EVENTS = "/api/internal/events/messages";
    public static final String MESSAGE_EVENT_BY_EVENT_ID = "/api/internal/events/messages/{eventId}";

    public static final String MESSAGE_BY_CHANNEL_ID = "/api/internal/messages/{channelId}";
    public static final String MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID = "/api/internal/messages/{channelId}/{messageId}";

    /*
        Tiers
     */
    public static final String TIER_RESULTS_BY_TIER_ID = "/api/internal/tier/{tierId}/results/current";

    /*
        Automated creation
     */
    public static final String TEST_DAILY_CREATION_BY_CLUB_ID = "/api/internal/championships_creation/{clubId}/test_daily";
    public static final String TEST_MONTHLY_CREATION_BY_CLUB_ID = "/api/internal/championships_creation/{clubId}/test_monthly";

    public static final String COPY_CHAMPIONSHIP_TO_CLUB = "/api/external/copy_event/{fromClubId}";

    /*
        Misc
     */
    public static final String MERGE_CLUB = "/api/internal/merge";
    public static final String USER_OVERVIEW = "/api/internal/users/overview";
    public static final String USER_COMMUNITY_PROGRESSION = "/api/external/users/community/progression";

    public static final String ALL_DISCORD_CHANNEL_CONFIGURATION = "/api/internal/discord/configurations";
    public static final String DISCORD_CHANNEL_CONFIGURATION = "/api/internal/discord/channels/{channelId}/configuration";

    public static final String DISCORD_CHANNEL_SINGLE_CONFIGURATION = "/api/internal/discord/configurations/{configurationId}";

    public static final String DISCORD_CALLBACK = "/api/external/discord/callback";
    public static final String POSTMARK_WEBHOOK = "/api/external/hooks/postmark";
    public static final String DISCORD_INTEGRATION_AUTH = "/api/internal/discord/integration/auth";

    public static final String DISCORD_GUILDS = "/api/internal/discord/integration/guilds";
    public static final String DISCORD_AUTHENTICATION_STATUS = "/api/internal/discord/integration/authentication_status";
    public static final String DISCORD_REDIRECT = "/api/external/discord/redirect";
    public static final String DISCORD_INVITE_BOT = "/api/external/discord/invite_bot";

    public static final String DISCORD_MANAGE_SERVER = "/api/internal/discord/integration/guilds/{guildId}/can_manage";
    public static final String DISCORD_GUILD = "/api/internal/discord/integration/guilds/{guildId}";

    public static final String DISCORD_GUILD_CHANNELS = "/api/internal/discord/integration/guilds/{guildId}/channels";
    public static final String DISCORD_GUILD_MEMBERS = "/api/internal/discord/integration/guilds/{guildId}/members";
    public static final String DISCORD_GUILD_ADMINISTRATORS = "/api/internal/discord/integration/guilds/{guildId}/administrators";
    public static final String DISCORD_GUILD_ADMINISTRATORS_CRUD = "/api/internal/discord/integration/guilds/{guildId}/administrators/{userId}";

    public static final String SECURITY_USER = "/api/internal/security/user";
}
