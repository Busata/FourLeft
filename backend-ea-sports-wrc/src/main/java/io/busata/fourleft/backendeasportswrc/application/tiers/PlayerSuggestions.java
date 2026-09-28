package io.busata.fourleft.backendeasportswrc.application.tiers;

import io.busata.fourleft.api.easportswrc.models.TierPlayerTo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Player autocomplete over every club leaderboard entry we've synced: distinct (ssid, display name) pairs
 * whose name contains the query, names starting with it first. No index backs the substring match — a
 * sequential scan of club_leaderboard_entry, ~120ms on 1.6M rows — which is fine behind a debounced
 * admin-only input.
 */
@Service
@RequiredArgsConstructor
class PlayerSuggestions {

    static final int MIN_QUERY_LENGTH = 2;
    private static final int LIMIT = 10;

    private final JdbcTemplate jdbc;

    List<TierPlayerTo> suggest(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.length() < MIN_QUERY_LENGTH) {
            return List.of();
        }
        return jdbc.query("""
                        SELECT ssid, display_name
                        FROM club_leaderboard_entry
                        WHERE ssid IS NOT NULL AND lower(display_name) LIKE '%' || ? || '%'
                        GROUP BY ssid, display_name
                        ORDER BY min(CASE WHEN lower(display_name) LIKE ? || '%' THEN 0 ELSE 1 END), display_name
                        LIMIT ?
                        """,
                (rs, i) -> new TierPlayerTo(rs.getString("ssid"), rs.getString("display_name")),
                q, q, LIMIT);
    }
}
