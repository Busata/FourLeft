package io.busata.fourleft.schedules;

import io.busata.fourleft.domain.configuration.ClubConfiguration;
import io.busata.fourleft.domain.configuration.repository.ClubConfigurationRepository;
import io.busata.fourleft.endpoints.club.automated.service.ChampionshipCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="automated", havingValue="true", matchIfMissing = true)
public class AutomatedChampionshipSchedule {

    private final ClubConfigurationRepository clubConfigurationRepository;
    private final ChampionshipCreator championshipCreator;

    @PostConstruct
    public void init() {
        log.info("AUTOMATED CHAMPIONSHIP SCHEDULE -- ACTIVE");
    }

    @Scheduled(cron = "0 57 9 * * *", zone = "Europe/Brussels")
    public void refreshDaily() {
        log.info("Refreshing daily championship");

        clubConfigurationRepository.findAll()
                .stream()
                .filter(ClubConfiguration::isDaily)
                .map(ClubConfiguration::getClubId)
                .forEach(championshipCreator::refreshDaily);
    }

    @Scheduled(cron = "0 0 10 * * *", zone = "Europe/Brussels")
    public void createDaily() {
        log.info("Creating championship");
        clubConfigurationRepository.findAll()
                .stream()
                .filter(ClubConfiguration::isDaily)
                .map(ClubConfiguration::getClubId)
                .forEach(championshipCreator::createDailyChampionship);
    }

    @Scheduled(cron = "0 1 8 * * 1", zone = "Europe/Brussels")
    public void refreshWeekly() {
        log.info("Refreshing weekly championship");
        clubConfigurationRepository.findAll()
                .stream()
                .filter(ClubConfiguration::isMonthly)
                .map(ClubConfiguration::getClubId)
                .forEach(championshipCreator::refreshWeekly);
    }

    @Scheduled(cron = "0 5 8 * * 1", zone = "Europe/Brussels")
    public void createWeekly() {
        log.info("Creating weekly championship");
        clubConfigurationRepository.findAll()
                .stream()
                .filter(ClubConfiguration::isMonthly)
                .map(ClubConfiguration::getClubId)
                .forEach(championshipCreator::createWeeklyChampionship);
        ;
    }
}
