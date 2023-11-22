package io.busata.fourleftdiscord.eawrcsports;

import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubCreateConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient(name="easportswrcapi", url="${eawrcsportsbackend.url}")
public interface EAWRCBackendApi {


    @GetMapping("/api_v2/results/{channelId}/current")
    Optional<String> getCurrentResults(@PathVariable Long channelId);

    @GetMapping("/api_v2/results/{channelId}/previous")
    Optional<String> getPreviousResults(@PathVariable Long channelId);

    @GetMapping("/api_v2/results/{channelId}/standings")
    Optional<String> getStandings(@PathVariable Long channelId);

    @GetMapping("/api_v2/configuration/channels")
    List<DiscordClubConfigurationTo> getConfigurations();

    @GetMapping("/api_v2/events/{channelId}/summary")
    Optional<String> getSummary(@PathVariable Long channelId);

    @PostMapping("/api_v2/profile/request")
    ProfileUpdateRequestResultTo requestTrackingUpdate(@RequestBody ProfileUpdateRequestTo request);

    @PostMapping("/api_v2/configuration/channels")
    void createChannelConfiguration(@RequestBody DiscordClubCreateConfigurationTo request);

}
