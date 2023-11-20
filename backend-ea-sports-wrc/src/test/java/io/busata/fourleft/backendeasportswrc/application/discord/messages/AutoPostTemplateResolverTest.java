package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections.AutoPostMessageSummary;
import io.busata.fourleft.backendeasportswrc.domain.models.*;
import io.busata.fourleft.backendeasportswrc.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

class AutoPostTemplateResolverTest extends AbstractIntegrationTest {

    @Autowired
    AutoPostTemplateResolver autoPostTemplateResolver;

    @Test
    public void testResolver() {


        Event event = new Event(
                "1",
                "123",
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                1L,
                new EventSettings(
                        1L, "H2 RWD", 1L, "Weather", 1L, "Location", ""
                )
        );
        event.updateStages(List.of(
                new Stage("1","2",new StageSettings(0L, "Bio Bio", 0L, "", 0L, "", 0L, ""))
        ));
        String render = autoPostTemplateResolver.render(AutoPostMessageService.baseTemplate, new AutoPostMessageSummary(
                event,2, List.of(
                new ClubLeaderboardEntry("Busata", "1345", "1234", 1L, 1L,  1L, 0L, "Talbot Sunbeam", Duration.ofMinutes(5), Duration.ofSeconds(15), Duration.ofMinutes(10), Duration.ofMinutes(10), Duration.ofSeconds(5)),
                new ClubLeaderboardEntry("JamesF890", "1345", "1234", 2L, 1L,  1L, 0L, "Talbot Sunbeam", Duration.ofMinutes(5), Duration.ofSeconds(15), Duration.ofMinutes(10),Duration.ofMinutes(10), Duration.ofSeconds(5))
        )));

        System.out.println(render);
    }
}