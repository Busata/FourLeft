package io.busata.fourleft.api;

public class RoutesTo {

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

    /*
        Misc
     */
    public static final String POSTMARK_WEBHOOK = "/api/external/hooks/postmark";

    public static final String SECURITY_USER = "/api/internal/security/user";

}
