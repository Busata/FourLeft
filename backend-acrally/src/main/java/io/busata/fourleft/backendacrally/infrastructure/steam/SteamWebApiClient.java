package io.busata.fourleft.backendacrally.infrastructure.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.busata.fourleft.backendacrally.infrastructure.properties.AcrallyProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Reads public Steam profile data via the Steam Web API. Two endpoints, one key:
 * {@code GetPlayerSummaries} (persona/avatar/account age/visibility) and
 * {@code GetPlayerBans} (VAC/game/community bans — an "already banned elsewhere" signal).
 * Every call is best-effort: no key, a network error, or a private profile just yields empty.
 */
@Component
public class SteamWebApiClient {

    private static final String BASE = "https://api.steampowered.com";

    private final RestClient restClient;
    private final String apiKey;

    public SteamWebApiClient(AcrallyProperties properties) {
        this.apiKey = properties.steam() == null ? null : properties.steam().webApiKey();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(6));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public boolean isEnabled() {
        return StringUtils.hasText(apiKey);
    }

    public Optional<Summary> fetchSummary(String steamId64) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        SummariesEnvelope envelope = restClient.get()
                .uri(BASE + "/ISteamUser/GetPlayerSummaries/v2/?key={key}&steamids={id}", apiKey, steamId64)
                .retrieve()
                .body(SummariesEnvelope.class);

        if (envelope == null || envelope.response() == null || envelope.response().players() == null
                || envelope.response().players().isEmpty()) {
            return Optional.empty();
        }
        Player player = envelope.response().players().getFirst();
        LocalDateTime created = player.timeCreated() == null
                ? null
                : LocalDateTime.ofEpochSecond(player.timeCreated(), 0, ZoneOffset.UTC);
        return Optional.of(new Summary(
                player.personaName(),
                player.avatarFull(),
                player.profileUrl(),
                created,
                player.communityVisibilityState()));
    }

    public Optional<Bans> fetchBans(String steamId64) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        BansEnvelope envelope = restClient.get()
                .uri(BASE + "/ISteamUser/GetPlayerBans/v1/?key={key}&steamids={id}", apiKey, steamId64)
                .retrieve()
                .body(BansEnvelope.class);

        if (envelope == null || envelope.players() == null || envelope.players().isEmpty()) {
            return Optional.empty();
        }
        BanEntry entry = envelope.players().getFirst();
        return Optional.of(new Bans(entry.vacBanned(), entry.numberOfGameBans(), entry.communityBanned()));
    }

    /** Normalised summary the rest of the app consumes. */
    public record Summary(String personaName, String avatarUrl, String profileUrl,
                          LocalDateTime accountCreated, Integer visibilityState) {
    }

    public record Bans(boolean vacBanned, int gameBanCount, boolean communityBanned) {
    }

    // ---- raw Steam Web API shapes ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SummariesEnvelope(Response response) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Response(List<Player> players) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Player(
            @JsonProperty("personaname") String personaName,
            @JsonProperty("avatarfull") String avatarFull,
            @JsonProperty("profileurl") String profileUrl,
            @JsonProperty("timecreated") Long timeCreated,
            @JsonProperty("communityvisibilitystate") Integer communityVisibilityState) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BansEnvelope(List<BanEntry> players) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BanEntry(
            @JsonProperty("VACBanned") boolean vacBanned,
            @JsonProperty("NumberOfGameBans") int numberOfGameBans,
            @JsonProperty("CommunityBanned") boolean communityBanned) {
    }
}
