package io.busata.fourleft.endpoints.club.automated;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.endpoints.club.automated.service.ClubChampionshipCopier;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipCreateRequestTo;
import io.busata.fourleft.endpoints.club.automated.service.DailyChampionshipCreator;
import io.busata.fourleft.endpoints.club.automated.service.WeeklyChampionshipCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChampionshipCreatorEndpoint {
    private final DailyChampionshipCreator dailyCreator;
    private final WeeklyChampionshipCreator monthlyCreator;


    @GetMapping(Routes.TEST_DAILY_CREATION_BY_CLUB_ID)
    public DR2ChampionshipCreateRequestTo testDailyCreation(@PathVariable long clubId) {
        return dailyCreator.createEvent(clubId);
    }
    @GetMapping(Routes.TEST_MONTHLY_CREATION_BY_CLUB_ID)
    public DR2ChampionshipCreateRequestTo testMonthly(@PathVariable long clubId) {
        return monthlyCreator.createEvent(clubId);
    }



}
