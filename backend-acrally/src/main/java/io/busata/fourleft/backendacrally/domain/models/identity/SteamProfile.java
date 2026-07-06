package io.busata.fourleft.backendacrally.domain.models.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "steam_profile")
@Getter
@NoArgsConstructor
public class SteamProfile {

    @Id
    @Column(name = "steam_id64")
    private String steamId64;

    @Column(name = "persona_name")
    private String personaName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "account_created")
    private LocalDateTime accountCreated;

    @Column(name = "visibility_state")
    private Integer visibilityState;

    @Column(name = "vac_banned", nullable = false)
    private boolean vacBanned;

    @Column(name = "game_ban_count", nullable = false)
    private int gameBanCount;

    @Column(name = "community_banned", nullable = false)
    private boolean communityBanned;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public SteamProfile(String steamId64) {
        this.steamId64 = steamId64;
    }

    /** Overwrites the snapshot with a freshly fetched one. */
    public void apply(String personaName, String avatarUrl, String profileUrl,
                      LocalDateTime accountCreated, Integer visibilityState,
                      boolean vacBanned, int gameBanCount, boolean communityBanned,
                      LocalDateTime fetchedAt) {
        this.personaName = personaName;
        this.avatarUrl = avatarUrl;
        this.profileUrl = profileUrl;
        this.accountCreated = accountCreated;
        this.visibilityState = visibilityState;
        this.vacBanned = vacBanned;
        this.gameBanCount = gameBanCount;
        this.communityBanned = communityBanned;
        this.fetchedAt = fetchedAt;
    }
}
