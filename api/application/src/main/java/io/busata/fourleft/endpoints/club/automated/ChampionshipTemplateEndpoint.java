package io.busata.fourleft.endpoints.club.automated;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.endpoints.club.automated.service.ClubChampionshipCopier;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ChampionshipTemplateEndpoint {
    private final ClubChampionshipCopier clubChampionshipCopier;

    @GetMapping(Routes.COPY_CHAMPIONSHIP_TO_CLUB)
    public String createEventTemplate(@PathVariable long fromClubId, Model model) {
        DR2ChampionshipCreateRequestTo template = clubChampionshipCopier.createCopyTemplate(fromClubId);

        model.addAttribute("request", template);

        return "event_copier";
    }
}
