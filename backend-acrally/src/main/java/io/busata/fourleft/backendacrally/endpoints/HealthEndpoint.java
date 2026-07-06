package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.HealthTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthEndpoint {

    @GetMapping("/acrally-api/health")
    public HealthTo health() {
        return new HealthTo("backend-acrally", "ok");
    }

}
