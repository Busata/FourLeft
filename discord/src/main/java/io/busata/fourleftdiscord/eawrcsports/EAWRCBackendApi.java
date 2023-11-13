package io.busata.fourleftdiscord.eawrcsports;

import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
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


    @GetMapping("/api_v2/results/{clubId}/current")
    Optional<String> getCurrentResults(@PathVariable String clubId);

    @GetMapping("/api_v2/results/{clubId}/previous")
    Optional<String> getPreviousResults(@PathVariable String clubId);

    @GetMapping("/api_v2/results/{clubId}/standings")
    Optional<String> getStandings(@PathVariable String clubId);

    @GetMapping("/api_v2/configuration/channels")
    List<DiscordClubConfigurationTo> getConfigurations();

    @GetMapping("/api_v2/events/{clubId}/summary")
    Optional<String> getSummary(@PathVariable String clubId);

    @PostMapping("/api_v2/profile/request")
    ProfileUpdateRequestResultTo requestTrackingUpdate(@RequestBody ProfileUpdateRequestTo request);

}
