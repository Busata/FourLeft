package io.busata.fourleft.endpoints.discord.configuration;

import io.busata.fourleft.api.models.configuration.ClubViewTo;
import io.busata.fourleft.api.models.configuration.PointSystemTo;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleft.api.models.configuration.DefaultPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.FixedPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.PointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.results.ConcatenationViewTo;
import io.busata.fourleft.api.models.configuration.results.MergedViewTo;
import io.busata.fourleft.api.models.configuration.results.RacenetFilterTo;
import io.busata.fourleft.api.models.configuration.results.PartitionViewTo;
import io.busata.fourleft.api.models.configuration.results.ResultsViewTo;
import io.busata.fourleft.api.models.configuration.results.SingleClubViewTo;
import io.busata.fourleft.domain.views.configuration.ClubView;
import io.busata.fourleft.domain.views.configuration.DiscordChannelConfiguration;
import io.busata.fourleft.domain.views.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.views.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.views.configuration.points.PointSystem;
import io.busata.fourleft.domain.views.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.views.configuration.results_views.ConcatenationView;
import io.busata.fourleft.domain.views.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.views.configuration.results_views.RacenetFilter;
import io.busata.fourleft.domain.views.configuration.results_views.PartitionView;
import io.busata.fourleft.domain.views.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.views.configuration.results_views.SingleClubView;
import io.busata.fourleft.infrastructure.common.Factory;

import java.util.List;

@Factory
public class DiscordChannelConfigurationFactory {

    public DiscordChannelConfiguration create(Long channelId, DiscordChannelConfigurationTo createDiscordChannelConfigurationTo) {
        ClubView clubView = createClubView(createDiscordChannelConfigurationTo.clubView());

        return new DiscordChannelConfiguration(
                null,
                channelId,
                "",
                clubView,
                List.of(clubView),
                createDiscordChannelConfigurationTo.enableAutoposts() ? List.of(clubView) : List.of()
        );

    }

    private ClubView createClubView(ClubViewTo clubView) {

        return new ClubView(
                null,
                clubView.badgeType(),
                createResultsView(clubView.resultsView()),
                createPointsView(clubView.pointsView())
        );

    }

    private ResultsView createResultsView(ResultsViewTo resultsView) {
        return switch(resultsView) {
            case (SingleClubViewTo clubViewTo) -> createSingleClubView(clubViewTo);
            case (MergedViewTo clubViewTo) -> createMergedResultsView(clubViewTo);
            case (PartitionViewTo clubViewTo) -> createPartitionView(clubViewTo);
            case (ConcatenationViewTo clubViewTo) -> createConcatenationView(clubViewTo);
            default -> throw new IllegalArgumentException("Unsupported");
        };
    }



    private PointsCalculator createPointsView(PointsCalculatorTo pointsView) {
        return switch(pointsView) {
            case (DefaultPointsCalculatorTo $) -> createDefaultPointsCalculator();
            case (FixedPointsCalculatorTo pointsCalculatorTo) -> createFixedPointsCalculator(pointsCalculatorTo);
            default -> throw new IllegalArgumentException("Unsupported");
        };
    }

    private PointsCalculator createFixedPointsCalculator(FixedPointsCalculatorTo pointsCalculatorTo) {
        return new FixedPointsCalculator(
                pointsCalculatorTo.getJoinChampionshipsCount(),
                pointsCalculatorTo.getOffsetChampionship(),
                    createPointSystem(pointsCalculatorTo.getPointSystem())
                );
    }

    private PointSystem createPointSystem(PointSystemTo pointsView) {
        return new PointSystem(
                null,
                "",
                pointsView.defaultStandingPoint(),
                pointsView.defaultPowerstagePoint(),
                pointsView.standingPoints(),
                pointsView.powerStagePoints()
        );
    }

    private PointsCalculator createDefaultPointsCalculator() {
        return new DefaultPointsCalculator();
    }

    private PartitionView createPartitionView(PartitionViewTo clubViewTo) {
        return new PartitionView(
                createResultsView(clubViewTo.getResultsView()),
                clubViewTo.getPartitionElements().stream().map(this::createRacenetFilter).toList()
        );
    }

    private RacenetFilter createRacenetFilter(RacenetFilterTo racenetFilterTo) {
        return new RacenetFilter(
                racenetFilterTo.getName(),
                racenetFilterTo.getFilterMode(),
                racenetFilterTo.getRacenetNames(),
                racenetFilterTo.isEnabled()
        );
    }

    private SingleClubView createSingleClubView(SingleClubViewTo clubViewTo) {
        return new SingleClubView(
                clubViewTo.getClubId(),
                clubViewTo.getName(),
                        clubViewTo.isUsePowerstage() ? List.of(clubViewTo.getPowerStageIndex()) : List.of(),
                createRacenetFilter(clubViewTo.getRacenetFilter())
        );
    }


    private MergeResultsView createMergedResultsView(MergedViewTo clubViewTo) {
        return new MergeResultsView(
                clubViewTo.getName(),
                clubViewTo.getMergeMode(),
                clubViewTo.getResultViews().stream().map(this::createSingleClubView).toList(),
                createRacenetFilter(clubViewTo.getRacenetFilter())
        );
    }

    private ConcatenationView createConcatenationView(ConcatenationViewTo clubViewTo) {
        return new ConcatenationView(
                clubViewTo.getName(),
                clubViewTo.getResultViews().stream().map(this::createSingleClubView).toList()
        );
    }

}
