package io.busata.fourleft.domain.options.models;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.busata.fourleft.domain.options.models.StageConditionOption.*;

@Slf4j
public enum CountryOption {

    MONTE_CARLO("eFrance","Monte Carlo",true, List.of(MIDDAY_DRY,
            SUNSET_DRY,
            SUNSET_LIGHT_SNOW,
            DUSK_DRY,
            NIGHT_DRY,
            NIGHT_LIGHT_SNOW)),
    WALES("eWales","Wales",true, List.of(
            MIDDAY_DRY,
            MIDDAY_RAIN,
            SUNSET_DRY,
            SUNSET_WET,
            NIGHT_DRY,
            NIGHT_RAIN

    )),
    GREECE("eGreece","Greece",true, List.of(
            MIDDAY_DRY,
            MIDDAY_OVERCAST,
            NIGHT_DRY,
            SUNSET_DRY,
            SUNSET_RAIN,
            DUSK_DRY

    )),
    GERMANY("eGermany","Germany",true,  List.of(
            MIDDAY_DRY,
            MIDDAY_RAIN,
            SUNSET_DRY,
            SUNSET_RAIN,
            DUSK_DRY,
            NIGHT_DRY
    )),
    FINLAND("eFinland","Finland",true,List.of(
            MIDDAY_DRY,
            MIDDAY_OVERCAST,
            SUNSET_DRY,
            DUSK_WET,
            DUSK_OVERCAST,
            NIGHT_DRY,
            MIDDAY_RAIN,
            DUSK_RAIN
    )),
    SWEDEN("eSweden","Sweden",true, List.of(
            SWEDEN_DAYTIME_DRY,
            MIDDAY_SNOW,
            SWEDEN_SUNSET_DRY,
            SUNSET_SNOW,
            SWEDEN_DUSK_DRY,
            SWEDEN_NIGHT_DRY,
            NIGHT_SNOW
    )),
    SPAIN("eSpain","Spain",false,List.of(
            MIDDAY_DRY,
            MIDDAY_WET,
            SUNSET_OVERCAST,
            DUSK_DRY,
            DUSK_WET,
            NIGHT_DRY,
            SUNSET_OVERCAST_WET,
            SUNSET_OVERCAST_LIGHT_RAIN,
            MIDDAY_WET_SHOWERS,
            DUSK_WET_SHOWERS

    )),
    AUSTRALIA("eAustralia","Australia",false, List.of(
            MIDDAY_DRY,
            MIDDAY_WET,
            SUNSET_DRY,
            SUNSET_OVERCAST,
            DUSK_DRY,
            NIGHT_DRY,
            MIDDAY_WET_SHOWERS,
            MIDDAY_WET_LIGHT_RAIN,
            SUNSET_OVERCAST_WET
    )),
    NEW_ZEALAND("eNewZealand","New Zealand",false, List.of(
            MIDDAY_DRY,
            MIDDAY_WET,
            SUNSET_DRY,
            DUSK_RAIN,
            NIGHT_DRY,
            NIGHT_WET,
            DUSK_LIGHT_RAIN,
            MIDDAY_WET_SHOWERS,
            NIGHT_WET_SHOWERS
    )),
    ARGENTINA("eTraslasierraMountains","Argentina",false, List.of(
            MIDDAY_DRY,
            MIDDAY_OVERCAST,
            SUNSET_DRY,
            DUSK_DRY,
            DUSK_RAIN,
            NIGHT_DRY,
            MIDDAY_OVERCAST_SHOWERS,
            MIDDAY_OVERCAST_RAIN
    )),
    POLAND("ePoland","Poland",false, List.of(
            MIDDAY_DRY,
            MIDDAY_RAIN,
            SUNSET_WET,
            DUSK_DRY,
            NIGHT_DRY,
            NIGHT_RAIN,
            SUNSET_WET_SHOWERS,
            MIDDAY_RAIN_DRY_SURFACE,
            NIGHT_RAIN_DRY_SURFACE,
            SUNSET_WET_DRY_SURFACE
    )),
    USA("eUsa","USA",false, List.of(
            MIDDAY_DRY,
            MIDDAY_WET,
            SUNSET_WET,
            DUSK_DRY,
            NIGHT_DRY,
            SUNSET_WET_SHOWERS,
            MIDDAY_WET_SHOWERS,
            NIGHT_RAIN_SHOWERS,
            NIGHT_RAIN_DRY_SURFACE
    )),
    SCOTLAND("eScotlandGravel","Scotland",true, List.of(
            MIDDAY_DRY,
            MIDDAY_RAIN,
            SUNRISE_DRY,
            DUSK_RAIN,
            NIGHT_DRY,
            NIGHT_RAIN,
            MIDDAY_WET,
            SUNSET_RAIN
    ))
    ;

    @Getter
    private final String id;
    public final String displayName;
    @Getter
    private final List<StageConditionOption> stageConditionOptions;
    public final boolean isDLC;

    CountryOption(String id, String displayName, boolean isDlc, List<StageConditionOption> stageConditionOptions) {
        this.id = id;
        this.displayName = displayName;
        this.isDLC = isDlc;
        this.stageConditionOptions = stageConditionOptions;
    }

    public static CountryOption findById(String id) {
        return Arrays.stream(CountryOption.values()).filter(x -> x.getId().equalsIgnoreCase(mapCountryAliasId(id))).findFirst().orElseThrow();
    }

    public List<StageConditionOption> getDryConditions() {
        return stageConditionOptions.stream().filter(StageConditionOption::isDry).collect(Collectors.toList());
    }
    public List<StageConditionOption> getWetConditions() {
        return stageConditionOptions.stream().filter(StageConditionOption::isDry).collect(Collectors.toList());
    }

    public static String mapCountryAliasId(String name) {
        return switch (name) {
            case "eMonaco" -> "eFrance";
            case "eUsa" -> "eUsa";
            case "31" -> "eScotlandGravel";
            case "eArgentina" -> "eTraslasierraMountains";
            default -> name;
        };
    }
}
