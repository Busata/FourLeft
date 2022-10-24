package io.busata.fourleft.domain.options.models;

import lombok.Getter;

public enum StageConditionOption {
    SUNRISE_DRY("eSunriseDry","Sunset / Clear / Dry Surface", false),
    MIDDAY_DRY("eMiddayDry", "Daytime / Clear / Dry Surface", false),
    MIDDAY_WET("eMiddayWet", "Daytime / Cloudy / Wet Surface", false),
    MIDDAY_WET_SHOWERS("eMiddayWetShowers", "Daytime / Showers / Wet Surface", true),
    MIDDAY_WET_LIGHT_RAIN("eMiddayWetLightRain", "Daytime / Rain / Wet Surface", true),
    MIDDAY_OVERCAST("eMiddayOvercast", "Daytime / Overcast / Dry Surface", false),
    MIDDAY_OVERCAST_SHOWERS("eMiddayOvercastShowers", "Daytime / Light Showers / Wet Surface", true),
    MIDDAY_OVERCAST_RAIN("eMiddayOvercastRain", "Daytime / Light Rain / Wet Surface", true),
    MIDDAY_RAIN("eMiddayRain", "Daytime / Heavy Rain / Wet Surface", true),
    MIDDAY_RAIN_DRY_SURFACE("eMiddayRainSurfaceDry", "Daytime / Overcast / Dry Surface", false),
    MIDDAY_SNOW("eMiddaySnow", "Daytime / Heavy Snow / Snow", true),

    DUSK_DRY("eDuskDry", "Dusk / Cloudy / Dry Surface", false),
    DUSK_WET("eDuskWet","Dusk / Cloudy / Wet Surface", false),
    DUSK_WET_SHOWERS("eDuskWetShowers","Dusk / Showers / Wet Surface", true),
    DUSK_RAIN("eDuskRain","Dusk / Heavy Rain / Wet Surface", true),
    DUSK_LIGHT_RAIN("eDuskRainLight","Dusk / Light Rain / Wet Surface", true),
    DUSK_OVERCAST("eDuskOvercast","Dusk / Overcast / Dry Surface", false),

    SUNSET_DRY("eSunsetDry", "Sunset / Cloudy / Dry Surface", false),
    SUNSET_OVERCAST("eSunsetOvercast", "Sunset / Overcast / Dry Surface", false),
    SUNSET_OVERCAST_WET("eSunsetOvercastSurfaceWet", "Sunset / Cloudy / Wet Surface", false),
    SUNSET_OVERCAST_LIGHT_RAIN("eSunsetOvercastLightRain", "Sunset / Light Rain / Wet Surface", true),
    SUNSET_WET("eSunsetWet", "Sunset / Cloudy / Wet Surface", false),
    SUNSET_WET_DRY_SURFACE("eSunsetWetSurfaceDry", "Sunset / Cloudy / Dry Surface", false),
    SUNSET_WET_SHOWERS("eSunsetWetShowers", "Sunset / Light Showers / Wet Surface", true),

    SUNSET_LIGHT_SNOW("eSunsetLightSnow", "Sunset / Light Snow / Dry Surface", true),
    SUNSET_SNOW("eSunsetSnow", "Sunset / Heavy Snow / Snow", true),
    SUNSET_RAIN("eSunsetRain", "Sunset / Heavy Rain / Wet Surface", true),

    NIGHT_DRY("eNightDry", "Night / Clear / Dry Surface", false),
    NIGHT_WET("eNightWet","Night / Cloudy / Wet Surface", true),
    NIGHT_WET_SHOWERS("eNightWetShowers","Night / Light Showers / Wet Surface", true),
    NIGHT_RAIN_SHOWERS("eNightRainShowers","Night / Showers / Wet Surface", true),
    NIGHT_RAIN("eNightRain", "Night / Heavy Rain / Wet Surface", true),
    NIGHT_RAIN_DRY_SURFACE("eNightRainSurfaceDry", "Night / Cloudy / Dry Surface", true),
    NIGHT_LIGHT_SNOW("eNightLightSnow", "Night / Light Snow / Dry Surface", true),
    NIGHT_SNOW("eNightSnow", "Night / Heavy Snow / Snow", true),

    SWEDEN_DAYTIME_DRY("eSwedenDaytimeDry","Daytime / Cloudy / Snow", false),
    SWEDEN_SUNSET_DRY("eSwedenSunsetDry","Sunset / Partly Cloudy / Snow", false),
    SWEDEN_DUSK_DRY("eSwedenDuskDry","Dusk / Cloudy / Snow", false),
    SWEDEN_NIGHT_DRY("eSwedenNightDry", "Night / Cloudy / Snow", false);

    @Getter
    String conditionId;
    String displayName;

    @Getter
    boolean raining;

    StageConditionOption(String conditionId, String displayName, boolean raining) {
        this.conditionId = conditionId;
        this.displayName = displayName;
        this.raining = raining;
    }

    public boolean isDry() {
        return !raining;
    }
}
