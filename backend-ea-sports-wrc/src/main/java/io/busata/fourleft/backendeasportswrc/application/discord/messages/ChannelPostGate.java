package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lets a MIXED channel's merged post through once, after every one of its clubs reported the moment
 * (event ended, championship started). Backed by {@code discord_channel_post_gate}; see V034.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelPostGate {

    private static final String CLAIM = "*";

    private final JdbcTemplate jdbc;

    /**
     * Records the club's arrival; true for exactly one caller — the one completing {@code clubIds}.
     *
     * <p>Runs outside any transaction so every statement commits on its own: two clubs arriving at once
     * must see each other's arrival row, or both would wait forever. At worst both see the set complete,
     * and the claim row's primary key lets only one of them through.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean arrive(Long channelId, String postKey, String clubId, List<String> clubIds) {
        jdbc.update("INSERT INTO discord_channel_post_gate (channel_id, post_key, club_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                channelId, postKey, clubId);

        List<String> arrived = jdbc.queryForList(
                "SELECT club_id FROM discord_channel_post_gate WHERE channel_id = ? AND post_key = ? AND club_id <> ?",
                String.class, channelId, postKey, CLAIM);
        if (!arrived.containsAll(clubIds)) {
            log.info("Channel {} post {}: waiting for clubs {}", channelId, postKey,
                    clubIds.stream().filter(id -> !arrived.contains(id)).toList());
            return false;
        }

        return jdbc.update("INSERT INTO discord_channel_post_gate (channel_id, post_key, club_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                channelId, postKey, CLAIM) == 1;
    }
}
