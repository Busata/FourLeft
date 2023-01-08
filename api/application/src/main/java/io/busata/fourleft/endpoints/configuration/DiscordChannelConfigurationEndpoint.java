package io.busata.fourleft.endpoints.configuration;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.ClubViewRepository;
import io.busata.fourleft.domain.configuration.DiscordChannelConfiguration;
import io.busata.fourleft.domain.configuration.DiscordChannelConfigurationRepository;
import io.busata.fourleft.domain.configuration.points.*;
import io.busata.fourleft.domain.configuration.results_views.BadgeType;
import io.busata.fourleft.domain.configuration.results_views.CommunityChallengeView;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TiersViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.List.of;

@RestController
@RequiredArgsConstructor
public class DiscordChannelConfigurationEndpoint {

    private final DiscordChannelConfigurationRepository discordChannelConfigurationRepository;

    private final ClubViewRepository repository;

    private final TiersViewRepository tiersViewRepository;
    private final PointSystemRepository pointSystemRepository;
    private final PointsCalculatorRepository pointsCalculatorRepository;

    private final ConfigurationToFactory configurationToFactory;

    @PostMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    public void setup() {
        PointSystem grfHistoryExpertPointSystem = new PointSystem(
                UUID.randomUUID(),
                "GRF Expert Historic",
                2,
                0,
                of(
                        new PointPair(1, 40 ),
                        new PointPair(2, 36),
                        new PointPair(3, 33),
                        new PointPair(4, 30),
                        new PointPair(5, 28),
                        new PointPair(6, 26),
                        new PointPair(7, 24),
                        new PointPair(8, 22),
                        new PointPair(9, 20),
                        new PointPair(10, 18),
                        new PointPair(11, 17),
                        new PointPair(12, 16),
                        new PointPair(13, 15),
                        new PointPair(14, 14),
                        new PointPair(15, 13),
                        new PointPair(16, 12),
                        new PointPair(17, 11),
                        new PointPair(18, 10),
                        new PointPair(19, 9),
                        new PointPair(21, 8),
                        new PointPair(21, 7),
                        new PointPair(22, 6),
                        new PointPair(23, 5),
                        new PointPair(24, 4),
                        new PointPair(25, 3),
                        new PointPair(26, 2)
                ),
                of(
                        new PointPair(1, 5),
                        new PointPair(2, 4),
                        new PointPair(3, 3),
                        new PointPair(4, 2),
                        new PointPair(5, 1)
                )
        );

        grfHistoryExpertPointSystem = pointSystemRepository.save(grfHistoryExpertPointSystem);


        FixedPointsCalculator fixedPointsCalculator = new FixedPointsCalculator(1, null, grfHistoryExpertPointSystem);

        fixedPointsCalculator = pointsCalculatorRepository.save(fixedPointsCalculator);

        createGRFHistoryExpert(fixedPointsCalculator);
    }

    private void createGRFHistoryExpert(PointsCalculator pointsCalculator) {

        final var view = new SingleClubView(314967L, true, -1, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "GRF Expert Historic",
                view,
                pointsCalculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1055617663436599356L,"GRF Expert Historic", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);

    }

    private void createEstonianR5(PointsCalculator pointsCalculator) {

        final var view = new SingleClubView(413952L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Dirt Estonia R5",
                view,
                pointsCalculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1056967249421938750L,"Dirt Estonia R5", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);

    }

    private void createScandiDozerClub(PointsCalculator calculator) {
        final var view = new SingleClubView(415178L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Gravel Trap",
                view,
                calculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 966718547533398106L,"Gravel Trap", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }
    private void createEstonianClub(PointsCalculator pointsCalculator) {

        final var view = new SingleClubView(440056L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Wreckfest",
                view,
                pointsCalculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1039266428550905957L,"Wreckfest", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);

    }
    private void createEnduranceViewScots(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(217020L, false, 0, BadgeType.NONE, PlayerRestrictions.FILTER, of(
                "Busata",
                "Hello There",
                "Boring Damo",
                "malchrobertson",
                "greenkingfisher",
                "Martyn798",
                "Chalmers78",
                "Dunoon1956",
                "Cavernous Ennui"
        ));

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "View on endurance club for scottish discord",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);


        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1034062257983864842L,"GRF Championship", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }

    @GetMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    public List<DiscordChannelConfigurationTo> getConfigurations() {

        return discordChannelConfigurationRepository.findAll().stream().map(configuration -> {
            return new DiscordChannelConfigurationTo(
                    configuration.getId(),
                    configuration.getChannelId(),
                    configuration.getDescription(),
                    getClubViewTo(configuration.getCommandsClubView()),
                    configuration.getAutopostClubViews().stream().map(this::getClubViewTo).collect(Collectors.toList())
            );
        }).collect(Collectors.toList());
    }

    private ClubViewTo getClubViewTo(ClubView configuration) {
        return new ClubViewTo(
                configuration.getId(),
                configuration.getDescription(),
                configurationToFactory.create(configuration.getResultsView()),
                configurationToFactory.create(configuration.getPointsCalculator())
        );
    }

    private void createGRFSpecial(PointsCalculator pointsCalculator) {
        final var tiersView = tiersViewRepository.getById(UUID.fromString("7961e20d-6719-43d1-bc9b-00835d6df589"));


        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "GRF Special",
                tiersView,
                pointsCalculator
        );


        clubView= repository.save(clubView);


        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 831903667409125377L,"GRF Special", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }

    private void createGRFDaily(PointsCalculator pointsCalculator) {

        final var view = new SingleClubView(417474L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "GRF Daily",
                view,
                pointsCalculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 972203349107683388L,"GRF Daily", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);

    }

    private void createGRFChampionship(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(377197L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "GRF Championship",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);


        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 817405818349682729L,"GRF Championship", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }
    private void createCommunityChallengesView(PointsCalculator pointsCalculator) {
        final var view = new CommunityChallengeView(true, true, true, BadgeType.PERCENTAGE);

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Community Challenges",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 892369709780070410L, "Dirt Rally 2 Main Chat", clubView, of());

        discordChannelConfigurationRepository.save(discordChannelConfiguration);

    }

    private void createScottish(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(403287L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Scottish championship",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 930147296836993086L,"Scots", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);

    }

    private void createMaintMasterDaily(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(432100L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "MaintMaster SRD Daily",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1006497755209936966L, "MaintMaster SRD Daily", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }

    private void createDirtyWeeklies(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(179084L, false, 0, BadgeType.RANKED, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Dirty Weeklies",
                view,
                pointsCalculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 892373522473685054L, "Dirty Weeklies",clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }
    private void createUniteRally(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(438695L, false, 0, BadgeType.RANKED, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Unite Rally Trophy",
                view,
                pointsCalculator
        );

        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 964681257793495081L, "Unite Rally Trophy",clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }

    private void createDirtyDailies(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(418341L, false, 0, BadgeType.RANKED, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Dirty Dailies",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);

        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 977962740234747954L, "Dirty Dailies", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }

    private void createDirtyMonthlies(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(431561L, true, -1, BadgeType.RANKED, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "Dirty Monthlies",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);


        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1002942121709416588L, "Dirty Monthlies", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }


    private void createGRFOneShot(PointsCalculator pointsCalculator) {
        final var view = new SingleClubView(434300L, false, 0, BadgeType.NONE, PlayerRestrictions.NONE, of());

        ClubView clubView = new ClubView(
                UUID.randomUUID(),
                "GRF One Shot Weekly",
                view,
                pointsCalculator
        );


        clubView = repository.save(clubView);


        DiscordChannelConfiguration discordChannelConfiguration = new DiscordChannelConfiguration(
                UUID.randomUUID(), 1022546826836066334L, "GRF One Shot", clubView, of(clubView));

        discordChannelConfigurationRepository.save(discordChannelConfiguration);
    }


}
