package io.busata.fourleft.backendeasportswrc.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.busata.fourleft.api.easportswrc.models.ClubCreationAssistSummary;
import io.busata.fourleft.backendeasportswrc.domain.services.ClubCreationAssistService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ClubCreationAssistanceEndpoint {

    private final ClubCreationAssistService clubCreationAssistService;

    @GetMapping("/api_v2/results/{clubId}/suggestion")
    ClubCreationAssistSummary getStats(@PathVariable String clubId) {

        return clubCreationAssistService.createSummary(clubId);
    }
}
