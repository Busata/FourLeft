package io.busata.fourleft.backendacrally.domain.services.identity;

import io.busata.fourleft.backendacrally.domain.models.identity.SteamProfile;
import io.busata.fourleft.backendacrally.infrastructure.steam.SteamWebApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SteamProfileService {

    private final SteamWebApiClient webApiClient;
    private final SteamProfileRepository repository;

    /**
     * Fetches the Steam profile and upserts our snapshot. Best-effort: a missing key, an API
     * hiccup, or a private profile leaves any existing snapshot untouched and never throws —
     * profile enrichment must not break the link/login it hangs off.
     */
    @Transactional
    public void refresh(String steamId64) {
        if (!webApiClient.isEnabled()) {
            return;
        }
        try {
            Optional<SteamWebApiClient.Summary> summary = webApiClient.fetchSummary(steamId64);
            if (summary.isEmpty()) {
                log.debug("No Steam summary for {} (private profile or unknown id)", steamId64);
                return;
            }
            SteamWebApiClient.Bans bans = webApiClient.fetchBans(steamId64)
                    .orElse(new SteamWebApiClient.Bans(false, 0, false));

            SteamWebApiClient.Summary s = summary.get();
            SteamProfile profile = repository.findById(steamId64).orElseGet(() -> new SteamProfile(steamId64));
            profile.apply(
                    s.personaName(), s.avatarUrl(), s.profileUrl(), s.accountCreated(), s.visibilityState(),
                    bans.vacBanned(), bans.gameBanCount(), bans.communityBanned(),
                    LocalDateTime.now());
            repository.save(profile);
        } catch (Exception e) {
            log.warn("Steam profile refresh failed for {}: {}", steamId64, e.getMessage());
        }
    }

    /** Non-blocking refresh for hot paths like login where we don't want to wait on Steam. */
    @Async
    public void refreshAsync(String steamId64) {
        refresh(steamId64);
    }
}
