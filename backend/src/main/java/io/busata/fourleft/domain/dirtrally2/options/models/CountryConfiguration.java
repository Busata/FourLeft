package io.busata.fourleft.domain.dirtrally2.options.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public enum CountryConfiguration {

    ARGENTINA(CountryOption.ARGENTINA,
                    StageConfiguration.ARGENTINA_A_FWD,
                    StageConfiguration.ARGENTINA_A_REV,
                    StageConfiguration.ARGENTINA_B_FWD,
                    StageConfiguration.ARGENTINA_B_REV
    ),
    AUSTRALIA(CountryOption.AUSTRALIA,
                    StageConfiguration.AUSTRALIA_A_FWD,
                    StageConfiguration.AUSTRALIA_A_REV,
                    StageConfiguration.AUSTRALIA_B_FWD,
                    StageConfiguration.AUSTRALIA_B_REV
    ),
    FINLAND(CountryOption.FINLAND,
                    StageConfiguration.FINLAND_A_FWD,
                    StageConfiguration.FINLAND_A_REV,
                    StageConfiguration.FINLAND_B_FWD,
                    StageConfiguration.FINLAND_B_REV
    ),
    GERMANY(CountryOption.GERMANY,
                    StageConfiguration.GERMANY_A_FWD,
                    StageConfiguration.GERMANY_A_REV,
                    StageConfiguration.GERMANY_B_FWD,
                    StageConfiguration.GERMANY_B_REV
    ),
    GREECE(CountryOption.GREECE,
                    StageConfiguration.GREECE_A_FWD,
                    StageConfiguration.GREECE_A_REV,
                    StageConfiguration.GREECE_B_FWD,
                    StageConfiguration.GREECE_B_REV
    ),

    MONTE_CARLO(CountryOption.MONTE_CARLO,
                    StageConfiguration.MONTE_CARLO_A_FWD,
                    StageConfiguration.MONTE_CARLO_A_REV,
                    StageConfiguration.MONTE_CARLO_B_FWD,
                    StageConfiguration.MONTE_CARLO_B_REV
    ),

    POLAND(CountryOption.POLAND,
                    StageConfiguration.POLAND_A_FWD,
                    StageConfiguration.POLAND_A_REV,
                    StageConfiguration.POLAND_B_FWD,
                    StageConfiguration.POLAND_B_REV
    ),

    SPAIN(CountryOption.SPAIN,
                    StageConfiguration.SPAIN_A_FWD,
                    StageConfiguration.SPAIN_A_REV,
                    StageConfiguration.SPAIN_B_FWD,
                    StageConfiguration.SPAIN_B_REV
    ),

    SCOTLAND(CountryOption.SCOTLAND,
                    StageConfiguration.SCOTLAND_A_FWD,
                    StageConfiguration.SCOTLAND_A_REV,
                    StageConfiguration.SCOTLAND_B_FWD,
                    StageConfiguration.SCOTLAND_B_REV
    ),

    SWEDEN(CountryOption.SWEDEN,
                    StageConfiguration.SWEDEN_A_FWD,
                    StageConfiguration.SWEDEN_A_REV,
                    StageConfiguration.SWEDEN_B_FWD,
                    StageConfiguration.SWEDEN_B_REV
    ),

    USA(CountryOption.USA,
                    StageConfiguration.USA_A_FWD,
                    StageConfiguration.USA_A_REV,
                    StageConfiguration.USA_B_FWD,
                    StageConfiguration.USA_B_REV
    ),
    WALES(CountryOption.WALES,
                    StageConfiguration.WALES_A_FWD,
                    StageConfiguration.WALES_A_REV,
                    StageConfiguration.WALES_B_FWD,
                    StageConfiguration.WALES_B_REV
    ),

    NEW_ZEALAND(CountryOption.NEW_ZEALAND,
                    StageConfiguration.NEW_ZEALAND_A_FWD,
                    StageConfiguration.NEW_ZEALAND_A_REV,
                    StageConfiguration.NEW_ZEALAND_B_FWD,
                    StageConfiguration.NEW_ZEALAND_B_REV
    );

    @Getter
    private final CountryOption country;

    @Getter
    private final StageConfiguration stageAForward;
    @Getter
    private final StageConfiguration stageAReverse;
    @Getter
    private final StageConfiguration stageBForward;
    @Getter
    private final StageConfiguration stageBReverse;

    CountryConfiguration(CountryOption country, StageConfiguration stageAForward, StageConfiguration stageAReverse, StageConfiguration stageBForward, StageConfiguration stageBReverse) {
        this.country = country;
        this.stageAForward = stageAForward;
        this.stageAReverse = stageAReverse;
        this.stageBForward = stageBForward;
        this.stageBReverse = stageBReverse;
    }

    public static CountryConfiguration findByCountry(CountryOption query) {
        return Arrays.stream(CountryConfiguration.values()).filter(countryConfiguration -> countryConfiguration.getCountry() == query).findFirst().orElseThrow();
    }

    public Optional<StageConfiguration> findReverse(StageConfiguration configuration) {
        if(this.stageAForward == configuration) {
            return Optional.of(this.stageAReverse);
        } else if (this.stageAReverse == configuration) {
            return Optional.of(this.stageAForward);
        } else if (this.stageBForward == configuration) {
            return Optional.of(this.stageBReverse);
        } else if (this.stageBReverse == configuration) {
            return Optional.of(this.stageBForward);
        } else {
            return Optional.empty();
        }
    }

    public List<StageOption> getStages() {
        List<StageOption> newList = new ArrayList<>();
        Stream.of(getStageAForward(), getStageBForward(), getStageAReverse(), getStageBReverse())
                .map(StageConfiguration::getStages).forEach(newList::addAll);
        return newList;
    }
}
