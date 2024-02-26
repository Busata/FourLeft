package io.busata.fourleft.backendeasportswrc.application.fieldmapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeatherMappings {
    private final static Map<String, Boolean> weatherMapping;
    static {
        Map<String, Boolean> initMap = new HashMap<>();

        initMap.put("Light Snowfall (Ice)", false);
        initMap.put("Partly Cloudy (Wet)", false);
        initMap.put("Heavy Rain (Wet)", false);
        initMap.put("Overcast (Dry)", true);
        initMap.put("Cloudy (Ice)", true);
        initMap.put("Cloudy (Dry)", true);
        initMap.put("Overcast (Snow)", false);
        initMap.put("Partly Cloudy (Dry)", true);
        initMap.put("Partly Cloudy (Snow)", false);
        initMap.put("Partly Cloudy (Ice)", true);
        initMap.put("Cloudy (Wet)", false);
        initMap.put("Light Rain (Wet)", false);
        initMap.put("Storm (Wet)", false);
        initMap.put("Heavy Snowfall (Snow)", false);
        initMap.put("Heavy Snowfall (Ice)", true);
        initMap.put("Clear (Snow)", true);
        initMap.put("Clear (Dry)", true);
        initMap.put("Clear (Ice)", false);
        initMap.put("Cloudy (Snow)", true);
        initMap.put("Overcast (Wet)", false);
        initMap.put("Overcast (Ice)", true);
        initMap.put("Light Snowfall (Snow)", false);

        weatherMapping = Collections.unmodifiableMap(initMap);
    }

    public static boolean isDry(String weatherAndSeason) {
        return weatherMapping.getOrDefault(weatherAndSeason, true);
    }

}
