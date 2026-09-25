package io.busata.fourleft.common;

/**
 * How a channel presents the clubs it tracks. Only {@code SINGLE} has behavior today: every channel-scoped
 * view (results, standings, stats, summary) reads the channel's primary club. {@code MIXED} and
 * {@code TIERED} are stored so multi-club channels can be configured ahead of their rendering.
 */
public enum ChannelClubMode {
    /** One club per channel — the original behavior. */
    SINGLE,
    /** Several clubs running the same championship (e.g. one per car class), shown interleaved. */
    MIXED,
    /** Several clubs forming divisions (e.g. "JRC 1", "JRC 2"), shown per tier in club order. */
    TIERED
}
