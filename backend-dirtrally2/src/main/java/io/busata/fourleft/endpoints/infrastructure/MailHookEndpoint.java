package io.busata.fourleft.endpoints.infrastructure;

import io.busata.fourleft.api.RoutesTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class MailHookEndpoint {
    @PostMapping(RoutesTo.POSTMARK_WEBHOOK)
    public void postmarkHook(@RequestBody String body) {
        log.info("Received Postmark, {}", body);
    }
}
