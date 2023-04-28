package io.busata.fourleft.api.messages;

public class QueueNames {
    public static final String MESSAGES_QUEUE = "q.messages";

    public static final String CLUB_EVENT_STARTED = "q.clubs.events.started";
    public static final String CLUB_EVENT_ENDED = "q.clubs.events.ended";
    public static final String COMMUNITY_UPDATED = "q.community.updated";
    public static final String LEADERBOARD_UPDATE = "q.clubs.leaderboard.updated";

    public static final String TICKER_ENTRIES_UPDATE = "q.ticker.entries.updated";

    private QueueNames() {}
}
