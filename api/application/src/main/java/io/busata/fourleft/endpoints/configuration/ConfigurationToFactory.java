package io.busata.fourleft.endpoints.configuration;

import io.busata.fourleft.api.models.configuration.CommunityChallengeViewTo;
import io.busata.fourleft.api.models.configuration.DefaultPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.FixedPointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.PointSystemTo;
import io.busata.fourleft.api.models.configuration.PointsCalculatorTo;
import io.busata.fourleft.api.models.configuration.ResultsViewTo;
import io.busata.fourleft.api.models.configuration.SingleClubViewTo;
import io.busata.fourleft.api.models.configuration.TiersViewTo;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointSystem;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.CommunityChallengeView;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TiersView;
import io.busata.fourleft.endpoints.club.tiers.service.TierFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConfigurationToFactory {

    private final TierFactory tierFactory;

    public ResultsViewTo create(ResultsView resultsView) {
        if(resultsView == null) {
            return null;
        }
        if (resultsView instanceof SingleClubView) {
            return createSingleClubViewTo((SingleClubView) resultsView);
        } else if (resultsView instanceof TiersView) {
            return createTiersViewTo((TiersView) resultsView);
        } else if (resultsView instanceof CommunityChallengeView) {
            return createCommunityChallegeViewTo((CommunityChallengeView) resultsView);
        } else {
            throw new IllegalArgumentException("Couldn't create a results view from this type");
        }
    }

    private SingleClubViewTo createSingleClubViewTo(SingleClubView resultsView) {
        return new SingleClubViewTo(resultsView.getClubId(), resultsView.getBadgeType(), resultsView.getPlayerRestriction(), resultsView.getPlayers());
    }
    private TiersViewTo createTiersViewTo(TiersView resultsView) {
        return new TiersViewTo(
                resultsView.isUsePowerStage(),
                resultsView.getDefaultPowerstageIndex(),
                resultsView.getTiers().stream().map(tierFactory::create).collect(Collectors.toList()));
    }
    private CommunityChallengeViewTo createCommunityChallegeViewTo(CommunityChallengeView resultsView) {
        return new CommunityChallengeViewTo(
                resultsView.isPostDailies(),
                resultsView.isPostWeeklies(),
                resultsView.isPostMonthlies(),
                resultsView.getBadgeType()

        );
    }

    public PointsCalculatorTo create(PointsCalculator pointsCalculator) {
        if(pointsCalculator == null) {
            return null;
        }
        if (pointsCalculator instanceof DefaultPointsCalculator) {
            return createDefaultPointsCalculatorTo((DefaultPointsCalculator) pointsCalculator);
        } else if (pointsCalculator instanceof FixedPointsCalculator) {
            return createFixedPointsCalculatorTo((FixedPointsCalculator) pointsCalculator);
        } else {
            throw new IllegalArgumentException("Couldn't create a results view from this type");
        }
    }

    private FixedPointsCalculatorTo createFixedPointsCalculatorTo(FixedPointsCalculator pointsCalculator) {
        return new FixedPointsCalculatorTo(
                pointsCalculator.getJoinChampionshipsCount(),
                pointsCalculator.getOffsetChampionship(),
                createPointSystemTo(pointsCalculator.getPointSystem())
        );
    }

    private PointSystemTo createPointSystemTo(PointSystem pointSystem) {
        return new PointSystemTo(
                pointSystem.getId(),
                pointSystem.getDescription(),
                pointSystem.getRankingPoints(),
                pointSystem.getPowerStagePoints()
        );
    }

    private DefaultPointsCalculatorTo createDefaultPointsCalculatorTo(DefaultPointsCalculator pointsCalculator) {
        return new DefaultPointsCalculatorTo();
    }
}
