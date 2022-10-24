package io.busata.fourleft.club;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2ChampionshipOptions;
import io.busata.fourleft.gateway.racenet.dto.club.championship.creation.DR2LocationOption;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class OptionGenerator {

    @Test
    @SneakyThrows
    public void generateCountryEnumLines() {
        DR2ChampionshipOptions options = new ObjectMapper().readValue(json, DR2ChampionshipOptions.class);
        options.locations().stream().map(location ->
                String.format("%s(\"%s\",\"%s\",%s),",cleanUpToUpperSnakeCase(location.name()),location.id(),location.name(),location.isDlc()))
                .forEach(System.out::println);
    }


    @Test
    @SneakyThrows
    public void generateLocationsEnumLines() {
        DR2ChampionshipOptions options = new ObjectMapper().readValue(json, DR2ChampionshipOptions.class);
        options.locations().stream().flatMap((DR2LocationOption dr2LocationOption) -> dr2LocationOption.routes().stream()).map(route ->
                String.format("%s(\"%s\",\"%s\",%s,%s),", cleanUpToUpperSnakeCase(route.name()),
                        route.id(), route.name(), route.lengthKm(), route.isDlc())).forEach(System.out::println);

    }

    private String cleanUpToUpperSnakeCase(String text) {
        return text
                .toUpperCase()
                .replace(" ", "_")
                .replace("'", "")
                .replace("’", "")
                .replace("-", "_")
                .replace("É", "E")
                .replace("Ę", "E")
                .replace("Ä", "A")
                .replace("À", "A")
                .replace("Ñ", "N")
                .replace("(", "_")
                .replace(")", "")
                .replace("Á", "A")
                .replace("Ö", "O")
                .replace("Ó", "O")
                .replace("Ł", "L")
                .replace("___", "_")
                .replace("__", "_");
    }

    private static String json = "{\n" +
            "  \"locations\":[\n" +
            "    {\n" +
            "      \"id\":\"eFrance\",\n" +
            "      \"name\":\"MONTE CARLO\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally01Route0\",\n" +
            "          \"name\":\"Pra d’Alart\",\n" +
            "          \"lengthKm\":9.831,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally01Route1\",\n" +
            "          \"name\":\"Col de Turini Départ\",\n" +
            "          \"lengthKm\":9.831,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally01Route2\",\n" +
            "          \"name\":\"Gordolon - Courte montée\",\n" +
            "          \"lengthKm\":5.175,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally01Route3\",\n" +
            "          \"name\":\"Col de Turini - Sprint en descente\",\n" +
            "          \"lengthKm\":4.73,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally01Route4\",\n" +
            "          \"name\":\"Col de Turini sprint en Montée\",\n" +
            "          \"lengthKm\":4.729,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally01Route5\",\n" +
            "          \"name\":\"Col de Turini - Descente\",\n" +
            "          \"lengthKm\":5.175,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally02Route0\",\n" +
            "          \"name\":\"Vallée descendante\",\n" +
            "          \"lengthKm\":10.866,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally02Route1\",\n" +
            "          \"name\":\"Route de Turini\",\n" +
            "          \"lengthKm\":10.866,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally02Route2\",\n" +
            "          \"name\":\"Col de Turini - Départ en descente\",\n" +
            "          \"lengthKm\":6.846,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally02Route3\",\n" +
            "          \"name\":\"Approche du Col de Turini - Montée\",\n" +
            "          \"lengthKm\":3.952,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally02Route4\",\n" +
            "          \"name\":\"Route de Turini Descente\",\n" +
            "          \"lengthKm\":3.952,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFranceRally02Route5\",\n" +
            "          \"name\":\"Route de Turini Montée\",\n" +
            "          \"lengthKm\":6.843,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetLightSnow\",\n" +
            "          \"name\":\"Sunset / Light Snow / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightLightSnow\",\n" +
            "          \"name\":\"Night / Light Snow / Dry Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eWales\",\n" +
            "      \"name\":\"POWYS\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally01Route0\",\n" +
            "          \"name\":\"Sweet Lamb\",\n" +
            "          \"lengthKm\":9.9,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally01Route1\",\n" +
            "          \"name\":\"Geufron Forest\",\n" +
            "          \"lengthKm\":10.0,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally01Route2\",\n" +
            "          \"name\":\"Pant Mawr\",\n" +
            "          \"lengthKm\":4.7,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally01Route3\",\n" +
            "          \"name\":\"Bidno Moorland Reverse\",\n" +
            "          \"lengthKm\":4.8,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally01Route4\",\n" +
            "          \"name\":\"Bidno Moorland\",\n" +
            "          \"lengthKm\":4.9,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally01Route5\",\n" +
            "          \"name\":\"Pant Mawr Reverse\",\n" +
            "          \"lengthKm\":5.1,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally02Route0\",\n" +
            "          \"name\":\"River Severn Valley\",\n" +
            "          \"lengthKm\":11.4,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally02Route1\",\n" +
            "          \"name\":\"Bronfelen\",\n" +
            "          \"lengthKm\":11.4,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally02Route2\",\n" +
            "          \"name\":\"Fferm Wynt\",\n" +
            "          \"lengthKm\":5.7,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally02Route3\",\n" +
            "          \"name\":\"Dyffryn Afon Reverse\",\n" +
            "          \"lengthKm\":5.7,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally02Route4\",\n" +
            "          \"name\":\"Dyffryn Afon\",\n" +
            "          \"lengthKm\":5.7,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eWalesRally02Route5\",\n" +
            "          \"name\":\"Fferm Wynt Reverse\",\n" +
            "          \"lengthKm\":5.7,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayRain\",\n" +
            "          \"name\":\"Daytime / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetWet\",\n" +
            "          \"name\":\"Sunset / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRain\",\n" +
            "          \"name\":\"Night / Heavy Rain / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eGreece\",\n" +
            "      \"name\":\"ARGOLIS\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally01Route1\",\n" +
            "          \"name\":\"Kathodo Leontiou\",\n" +
            "          \"lengthKm\":9.6,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally01Route2\",\n" +
            "          \"name\":\"Pomona Ékrixi\",\n" +
            "          \"lengthKm\":5.09,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally01Route3\",\n" +
            "          \"name\":\"Fourkéta Kourva\",\n" +
            "          \"lengthKm\":4.8,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally01Route4\",\n" +
            "          \"name\":\"Koryfi Dafni\",\n" +
            "          \"lengthKm\":4.5,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally01Route5\",\n" +
            "          \"name\":\"Ampelonas Ormi\",\n" +
            "          \"lengthKm\":4.95,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally02Route0\",\n" +
            "          \"name\":\"Perasma Platani\",\n" +
            "          \"lengthKm\":10.69,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally02Route1\",\n" +
            "          \"name\":\"Tsiristra Théa\",\n" +
            "          \"lengthKm\":10.36,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally02Route2\",\n" +
            "          \"name\":\"Ourea Spevsi\",\n" +
            "          \"lengthKm\":5.74,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally02Route3\",\n" +
            "          \"name\":\"Pedines Epidaxi\",\n" +
            "          \"lengthKm\":5.38,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally02Route4\",\n" +
            "          \"name\":\"Abies Koiláda\",\n" +
            "          \"lengthKm\":7.09,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally02Route5\",\n" +
            "          \"name\":\"Ypsona tou Dasos\",\n" +
            "          \"lengthKm\":6.59,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGreeceRally01Route0\",\n" +
            "          \"name\":\"Anodou Farmakas\",\n" +
            "          \"lengthKm\":9.6,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayOvercast\",\n" +
            "          \"name\":\"Daytime / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetRain\",\n" +
            "          \"name\":\"Sunset / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eGermany\",\n" +
            "      \"name\":\"BAUMHOLDER\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally01Route0\",\n" +
            "          \"name\":\"Oberstein\",\n" +
            "          \"lengthKm\":11.67,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally02Route0\",\n" +
            "          \"name\":\"Hammerstein\",\n" +
            "          \"lengthKm\":10.81,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally01Route1\",\n" +
            "          \"name\":\"Frauenberg\",\n" +
            "          \"lengthKm\":11.67,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally01Route2\",\n" +
            "          \"name\":\"Waldaufstieg\",\n" +
            "          \"lengthKm\":5.39,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally01Route3\",\n" +
            "          \"name\":\"Kreuzungsring Reverse\",\n" +
            "          \"lengthKm\":6.31,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally01Route4\",\n" +
            "          \"name\":\"Kreuzungsring\",\n" +
            "          \"lengthKm\":6.31,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally01Route5\",\n" +
            "          \"name\":\"Waldabstieg\",\n" +
            "          \"lengthKm\":5.39,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally02Route1\",\n" +
            "          \"name\":\"Ruschberg\",\n" +
            "          \"lengthKm\":10.7,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally02Route2\",\n" +
            "          \"name\":\"Verbundsring\",\n" +
            "          \"lengthKm\":5.85,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally02Route3\",\n" +
            "          \"name\":\"Innerer Feld-Sprint\",\n" +
            "          \"lengthKm\":5.56,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally02Route4\",\n" +
            "          \"name\":\"Innerer Feld-Sprint (umgekehrt)\",\n" +
            "          \"lengthKm\":5.56,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eGermanyRally02Route5\",\n" +
            "          \"name\":\"Verbundsring Reverse\",\n" +
            "          \"lengthKm\":5.85,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayRain\",\n" +
            "          \"name\":\"Daytime / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetRain\",\n" +
            "          \"name\":\"Sunset / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eFinland\",\n" +
            "      \"name\":\"JÄMSÄ\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally01Route0\",\n" +
            "          \"name\":\"Kontinjärvi\",\n" +
            "          \"lengthKm\":15.05,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally01Route1\",\n" +
            "          \"name\":\"Hämelahti\",\n" +
            "          \"lengthKm\":14.96,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally01Route2\",\n" +
            "          \"name\":\"Kailajärvi\",\n" +
            "          \"lengthKm\":7.51,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally01Route3\",\n" +
            "          \"name\":\"Jyrkysjärvi\",\n" +
            "          \"lengthKm\":7.55,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally01Route4\",\n" +
            "          \"name\":\"Naarajärvi\",\n" +
            "          \"lengthKm\":7.43,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally01Route5\",\n" +
            "          \"name\":\"Paskuri\",\n" +
            "          \"lengthKm\":7.34,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally02Route0\",\n" +
            "          \"name\":\"Kakaristo\",\n" +
            "          \"lengthKm\":16.2,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally02Route1\",\n" +
            "          \"name\":\"Pitkäjärvi\",\n" +
            "          \"lengthKm\":16.2,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally02Route2\",\n" +
            "          \"name\":\"Iso Oksjärvi\",\n" +
            "          \"lengthKm\":8.04,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally02Route3\",\n" +
            "          \"name\":\"Järvenkylä\",\n" +
            "          \"lengthKm\":8.05,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally02Route4\",\n" +
            "          \"name\":\"Kotajärvi\",\n" +
            "          \"lengthKm\":8.1,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eFinlandRally02Route5\",\n" +
            "          \"name\":\"Oksala\",\n" +
            "          \"lengthKm\":8.1,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayOvercast\",\n" +
            "          \"name\":\"Daytime / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskWet\",\n" +
            "          \"name\":\"Dusk / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskOvercast\",\n" +
            "          \"name\":\"Dusk / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayRain\",\n" +
            "          \"name\":\"Daytime / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskRain\",\n" +
            "          \"name\":\"Dusk / Heavy Rain / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eSweden\",\n" +
            "      \"name\":\"VÄRMLAND\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally01Route0\",\n" +
            "          \"name\":\"Ransbysäter\",\n" +
            "          \"lengthKm\":11.977,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally01Route1\",\n" +
            "          \"name\":\"Norraskoga\",\n" +
            "          \"lengthKm\":11.977,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally01Route2\",\n" +
            "          \"name\":\"Älgsjön Sprint\",\n" +
            "          \"lengthKm\":5.248,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally01Route3\",\n" +
            "          \"name\":\"Stor-jangen Sprint Reverse\",\n" +
            "          \"lengthKm\":6.693,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally01Route4\",\n" +
            "          \"name\":\"Stor-jangen Sprint\",\n" +
            "          \"lengthKm\":6.693,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally01Route5\",\n" +
            "          \"name\":\"Skogsrallyt\",\n" +
            "          \"lengthKm\":5.248,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally02Route0\",\n" +
            "          \"name\":\"Hamra\",\n" +
            "          \"lengthKm\":12.343,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally02Route1\",\n" +
            "          \"name\":\"Lysvik\",\n" +
            "          \"lengthKm\":12.343,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally02Route2\",\n" +
            "          \"name\":\"Elgsjön\",\n" +
            "          \"lengthKm\":7.278,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally02Route3\",\n" +
            "          \"name\":\"Björklangen\",\n" +
            "          \"lengthKm\":5.191,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally02Route4\",\n" +
            "          \"name\":\"Östra Hinnsjön\",\n" +
            "          \"lengthKm\":5.191,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenRally02Route5\",\n" +
            "          \"name\":\"Älgsjön\",\n" +
            "          \"lengthKm\":7.278,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eSwedenDaytimeDry\",\n" +
            "          \"name\":\"Daytime / Cloudy / Snow\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddaySnow\",\n" +
            "          \"name\":\"Daytime / Heavy Snow / Snow\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenSunsetDry\",\n" +
            "          \"name\":\"Sunset / Partly Cloudy / Snow\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetSnow\",\n" +
            "          \"name\":\"Sunset / Heavy Snow / Snow\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Snow\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSwedenNightDry\",\n" +
            "          \"name\":\"Night / Cloudy / Snow\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightSnow\",\n" +
            "          \"name\":\"Night / Heavy Snow / Snow\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eSpain\",\n" +
            "      \"name\":\"RIBADELLES\",\n" +
            "      \"isDlc\":false,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally01Route0\",\n" +
            "          \"name\":\"Comienzo De Bellriu\",\n" +
            "          \"lengthKm\":14.342,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally02Route0\",\n" +
            "          \"name\":\"Centenera\",\n" +
            "          \"lengthKm\":10.571,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally01Route1\",\n" +
            "          \"name\":\"Final de Bellriu\",\n" +
            "          \"lengthKm\":14.342,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally01Route2\",\n" +
            "          \"name\":\"Ascenso por valle el Gualet\",\n" +
            "          \"lengthKm\":7.002,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally01Route3\",\n" +
            "          \"name\":\"Viñedos dentro del valle Parra\",\n" +
            "          \"lengthKm\":6.813,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally01Route4\",\n" +
            "          \"name\":\"Ascenso bosque Montverd\",\n" +
            "          \"lengthKm\":6.813,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally01Route5\",\n" +
            "          \"name\":\"Salida desde Montverd\",\n" +
            "          \"lengthKm\":7.002,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally02Route1\",\n" +
            "          \"name\":\"Camino a Centenera\",\n" +
            "          \"lengthKm\":10.571,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally02Route2\",\n" +
            "          \"name\":\"Descenso por carretera\",\n" +
            "          \"lengthKm\":4.584,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally02Route3\",\n" +
            "          \"name\":\"Viñedos Dardenyà\",\n" +
            "          \"lengthKm\":6.549,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally02Route4\",\n" +
            "          \"name\":\"Viñedos Dardenyà inversa\",\n" +
            "          \"lengthKm\":6.549,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSpainRally02Route5\",\n" +
            "          \"name\":\"Subida por carretera\",\n" +
            "          \"lengthKm\":4.584,\n" +
            "          \"isDlc\":false\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWet\",\n" +
            "          \"name\":\"Daytime / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetOvercast\",\n" +
            "          \"name\":\"Sunset / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskWet\",\n" +
            "          \"name\":\"Dusk / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetOvercastSurfaceWet\",\n" +
            "          \"name\":\"Sunset / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetOvercastLightRain\",\n" +
            "          \"name\":\"Sunset / Light Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWetShowers\",\n" +
            "          \"name\":\"Daytime / Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskWetShowers\",\n" +
            "          \"name\":\"Dusk / Showers / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eAustralia\",\n" +
            "      \"name\":\"MONARO\",\n" +
            "      \"isDlc\":false,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally01Route0\",\n" +
            "          \"name\":\"Mount Kaye Pass\",\n" +
            "          \"lengthKm\":12.503,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally02Route0\",\n" +
            "          \"name\":\"Chandlers Creek\",\n" +
            "          \"lengthKm\":12.341,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally01Route1\",\n" +
            "          \"name\":\"Mount Kaye Pass Reverse\",\n" +
            "          \"lengthKm\":12.503,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally01Route2\",\n" +
            "          \"name\":\"Rockton Plains\",\n" +
            "          \"lengthKm\":6.888,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally01Route3\",\n" +
            "          \"name\":\"Yambulla Mountain Descent\",\n" +
            "          \"lengthKm\":6.64,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally01Route4\",\n" +
            "          \"name\":\"Yambulla Mountain Ascent\",\n" +
            "          \"lengthKm\":6.64,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally01Route5\",\n" +
            "          \"name\":\"Rockton Plains Reverse\",\n" +
            "          \"lengthKm\":6.888,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally02Route1\",\n" +
            "          \"name\":\"Chandlers Creek Reverse\",\n" +
            "          \"lengthKm\":12.341,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally02Route2\",\n" +
            "          \"name\":\"Noorinbee Ridge Ascent\",\n" +
            "          \"lengthKm\":5.277,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally02Route3\",\n" +
            "          \"name\":\"Taylor Farm Sprint\",\n" +
            "          \"lengthKm\":7.007,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally02Route4\",\n" +
            "          \"name\":\"Bondi Forest\",\n" +
            "          \"lengthKm\":7.007,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eAustraliaRally02Route5\",\n" +
            "          \"name\":\"Noorinbee Ridge Descent\",\n" +
            "          \"lengthKm\":5.277,\n" +
            "          \"isDlc\":false\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWet\",\n" +
            "          \"name\":\"Daytime / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetOvercast\",\n" +
            "          \"name\":\"Sunset / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWetShowers\",\n" +
            "          \"name\":\"Daytime / Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWetLightRain\",\n" +
            "          \"name\":\"Daytime / Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetOvercastSurfaceWet\",\n" +
            "          \"name\":\"Sunset / Cloudy / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eNewZealand\",\n" +
            "      \"name\":\"HAWKES BAY\",\n" +
            "      \"isDlc\":false,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally01Route0\",\n" +
            "          \"name\":\"Te Awanga Forward\",\n" +
            "          \"lengthKm\":11.48,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally02Route0\",\n" +
            "          \"name\":\"Waimarama Point Forward\",\n" +
            "          \"lengthKm\":16.057,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally01Route1\",\n" +
            "          \"name\":\"Ocean Beach\",\n" +
            "          \"lengthKm\":11.48,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally01Route2\",\n" +
            "          \"name\":\"Te Awanga Sprint Forward\",\n" +
            "          \"lengthKm\":4.79,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally01Route3\",\n" +
            "          \"name\":\"Ocean Beach Sprint Forward\",\n" +
            "          \"lengthKm\":6.613,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally01Route4\",\n" +
            "          \"name\":\"Ocean Beach Sprint Reverse\",\n" +
            "          \"lengthKm\":6.613,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally01Route5\",\n" +
            "          \"name\":\"Te Awanga Sprint Reverse\",\n" +
            "          \"lengthKm\":4.79,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally02Route1\",\n" +
            "          \"name\":\"Waimarama Point Reverse\",\n" +
            "          \"lengthKm\":16.057,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally02Route2\",\n" +
            "          \"name\":\"Elsthorpe Sprint Forward\",\n" +
            "          \"lengthKm\":7.317,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally02Route3\",\n" +
            "          \"name\":\"Waimarama Sprint Forward\",\n" +
            "          \"lengthKm\":8.807,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally02Route4\",\n" +
            "          \"name\":\"Waimarama Sprint Reverse\",\n" +
            "          \"lengthKm\":8.807,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNewZealandRally02Route5\",\n" +
            "          \"name\":\"Elsthorpe Sprint Reverse\",\n" +
            "          \"lengthKm\":7.317,\n" +
            "          \"isDlc\":false\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWet\",\n" +
            "          \"name\":\"Daytime / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskRain\",\n" +
            "          \"name\":\"Dusk / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightWet\",\n" +
            "          \"name\":\"Night / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskRainLight\",\n" +
            "          \"name\":\"Dusk / Light Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWetShowers\",\n" +
            "          \"name\":\"Daytime / Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightWetShowers\",\n" +
            "          \"name\":\"Night / Light Showers / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eTraslasierraMountains\",\n" +
            "      \"name\":\"CATAMARCA PROVINCE\",\n" +
            "      \"isDlc\":false,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally01Route0\",\n" +
            "          \"name\":\"Las Juntas\",\n" +
            "          \"lengthKm\":8.25,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally02Route0\",\n" +
            "          \"name\":\"Valle de los puentes\",\n" +
            "          \"lengthKm\":7.976,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally01Route1\",\n" +
            "          \"name\":\"Camino a La Puerta\",\n" +
            "          \"lengthKm\":8.25,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally01Route2\",\n" +
            "          \"name\":\"Camino de acantilados y rocas\",\n" +
            "          \"lengthKm\":5.3,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally01Route3\",\n" +
            "          \"name\":\"El Rodeo\",\n" +
            "          \"lengthKm\":2.842,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally01Route4\",\n" +
            "          \"name\":\"La Merced\",\n" +
            "          \"lengthKm\":2.842,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally01Route5\",\n" +
            "          \"name\":\"Camino de acantilados y rocas inverso\",\n" +
            "          \"lengthKm\":5.3,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally02Route1\",\n" +
            "          \"name\":\"Valle de los puentes a la inversa\",\n" +
            "          \"lengthKm\":7.976,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally02Route2\",\n" +
            "          \"name\":\"Miraflores\",\n" +
            "          \"lengthKm\":3.352,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally02Route3\",\n" +
            "          \"name\":\"San Isidro\",\n" +
            "          \"lengthKm\":4.48,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally02Route4\",\n" +
            "          \"name\":\"Camino a Coneta\",\n" +
            "          \"lengthKm\":4.48,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eArgentinaRally02Route5\",\n" +
            "          \"name\":\"Huillaprima\",\n" +
            "          \"lengthKm\":3.352,\n" +
            "          \"isDlc\":false\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayOvercast\",\n" +
            "          \"name\":\"Daytime / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskRain\",\n" +
            "          \"name\":\"Dusk / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayOvercastShowers\",\n" +
            "          \"name\":\"Daytime / Light Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayOvercastRain\",\n" +
            "          \"name\":\"Daytime / Light Rain / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"ePoland\",\n" +
            "      \"name\":\"ŁĘCZNA COUNTY\",\n" +
            "      \"isDlc\":false,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally01Route0\",\n" +
            "          \"name\":\"Zaróbka\",\n" +
            "          \"lengthKm\":16.461,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally01Route1\",\n" +
            "          \"name\":\"Zagórze\",\n" +
            "          \"lengthKm\":16.461,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally01Route2\",\n" +
            "          \"name\":\"Kopina\",\n" +
            "          \"lengthKm\":7.03,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally01Route3\",\n" +
            "          \"name\":\"Marynka\",\n" +
            "          \"lengthKm\":9.247,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally01Route4\",\n" +
            "          \"name\":\"Borysik\",\n" +
            "          \"lengthKm\":9.247,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally01Route5\",\n" +
            "          \"name\":\"Józefin\",\n" +
            "          \"lengthKm\":7.03,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally02Route0\",\n" +
            "          \"name\":\"Jezioro Rotcze\",\n" +
            "          \"lengthKm\":13.42,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally02Route1\",\n" +
            "          \"name\":\"Zienki\",\n" +
            "          \"lengthKm\":13.42,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally02Route2\",\n" +
            "          \"name\":\"Czarny Las\",\n" +
            "          \"lengthKm\":6.624,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally02Route3\",\n" +
            "          \"name\":\"Lejno\",\n" +
            "          \"lengthKm\":6.815,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally02Route4\",\n" +
            "          \"name\":\"Jagodno\",\n" +
            "          \"lengthKm\":6.815,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"ePolandRally02Route5\",\n" +
            "          \"name\":\"Jezioro Lukie\",\n" +
            "          \"lengthKm\":6.624,\n" +
            "          \"isDlc\":false\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayRain\",\n" +
            "          \"name\":\"Daytime / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetWet\",\n" +
            "          \"name\":\"Sunset / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRain\",\n" +
            "          \"name\":\"Night / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetWetShowers\",\n" +
            "          \"name\":\"Sunset / Light Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayRainSurfaceDry\",\n" +
            "          \"name\":\"Daytime / Overcast / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRainSurfaceDry\",\n" +
            "          \"name\":\"Night / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetWetSurfaceDry\",\n" +
            "          \"name\":\"Sunset / Cloudy / Dry Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eUsa\",\n" +
            "      \"name\":\"NEW ENGLAND\",\n" +
            "      \"isDlc\":false,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally01Route0\",\n" +
            "          \"name\":\"North Fork Pass\",\n" +
            "          \"lengthKm\":12.503,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally01Route1\",\n" +
            "          \"name\":\"North Fork Pass Reverse\",\n" +
            "          \"lengthKm\":12.503,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally01Route2\",\n" +
            "          \"name\":\"Hancock Creek Burst\",\n" +
            "          \"lengthKm\":6.888,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally01Route3\",\n" +
            "          \"name\":\"Fuller Mountain Descent\",\n" +
            "          \"lengthKm\":6.64,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally01Route4\",\n" +
            "          \"name\":\"Fuller Mountain Ascent\",\n" +
            "          \"lengthKm\":6.64,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally01Route5\",\n" +
            "          \"name\":\"Fury Lake Depart\",\n" +
            "          \"lengthKm\":6.888,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally02Route0\",\n" +
            "          \"name\":\"Beaver Creek Trail Forward\",\n" +
            "          \"lengthKm\":12.858,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally02Route1\",\n" +
            "          \"name\":\"Beaver Creek Trail Reverse\",\n" +
            "          \"lengthKm\":12.858,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally02Route2\",\n" +
            "          \"name\":\"Hancock Hill Sprint Forward\",\n" +
            "          \"lengthKm\":6.009,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally02Route3\",\n" +
            "          \"name\":\"Tolt Valley Sprint Reverse\",\n" +
            "          \"lengthKm\":6.101,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally02Route4\",\n" +
            "          \"name\":\"Tolt Valley Sprint Forward\",\n" +
            "          \"lengthKm\":6.101,\n" +
            "          \"isDlc\":false\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eUsaRally02Route5\",\n" +
            "          \"name\":\"Hancock Hill Sprint Reverse\",\n" +
            "          \"lengthKm\":6.009,\n" +
            "          \"isDlc\":false\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWet\",\n" +
            "          \"name\":\"Daytime / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetWet\",\n" +
            "          \"name\":\"Sunset / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskDry\",\n" +
            "          \"name\":\"Dusk / Cloudy / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRain\",\n" +
            "          \"name\":\"Night / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetWetShowers\",\n" +
            "          \"name\":\"Sunset / Light Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWetShowers\",\n" +
            "          \"name\":\"Daytime / Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRainShowers\",\n" +
            "          \"name\":\"Night / Showers / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRainSurfaceDry\",\n" +
            "          \"name\":\"Night / Cloudy / Dry Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eScotlandGravel\",\n" +
            "      \"name\":\"PERTH AND KINROSS\",\n" +
            "      \"isDlc\":true,\n" +
            "      \"routes\":[\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally03Route0\",\n" +
            "          \"name\":\"South Morningside\",\n" +
            "          \"lengthKm\":12.581,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally03Route1\",\n" +
            "          \"name\":\"South Morningside Reverse\",\n" +
            "          \"lengthKm\":12.664,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally03Route2\",\n" +
            "          \"name\":\"Old Butterstone Muir\",\n" +
            "          \"lengthKm\":5.82,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally03Route3\",\n" +
            "          \"name\":\"Rosebank Farm\",\n" +
            "          \"lengthKm\":7.16,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally03Route4\",\n" +
            "          \"name\":\"Rosebank Farm Reverse\",\n" +
            "          \"lengthKm\":6.964,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally03Route5\",\n" +
            "          \"name\":\"Old Butterstone Muir Reverse\",\n" +
            "          \"lengthKm\":5.664,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally04Route0\",\n" +
            "          \"name\":\"Newhouse Bridge\",\n" +
            "          \"lengthKm\":12.856,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally04Route1\",\n" +
            "          \"name\":\"Newhouse Bridge Reverse\",\n" +
            "          \"lengthKm\":12.98,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally04Route2\",\n" +
            "          \"name\":\"Glencastle Farm\",\n" +
            "          \"lengthKm\":5.247,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally04Route3\",\n" +
            "          \"name\":\"Annbank Station\",\n" +
            "          \"lengthKm\":7.767,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally04Route4\",\n" +
            "          \"name\":\"Annbank Station Reverse\",\n" +
            "          \"lengthKm\":7.586,\n" +
            "          \"isDlc\":true\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eScotlandRally04Route5\",\n" +
            "          \"name\":\"Glencastle Farm Reverse\",\n" +
            "          \"lengthKm\":5.24,\n" +
            "          \"isDlc\":true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"supportedConditions\":[\n" +
            "        {\n" +
            "          \"id\":\"eMiddayDry\",\n" +
            "          \"name\":\"Daytime / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayRain\",\n" +
            "          \"name\":\"Daytime / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunriseDry\",\n" +
            "          \"name\":\"Sunset / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eDuskRain\",\n" +
            "          \"name\":\"Dusk / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightDry\",\n" +
            "          \"name\":\"Night / Clear / Dry Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eNightRain\",\n" +
            "          \"name\":\"Night / Heavy Rain / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eMiddayWet\",\n" +
            "          \"name\":\"Daytime / Cloudy / Wet Surface\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\":\"eSunsetRain\",\n" +
            "          \"name\":\"Sunset / Heavy Rain / Wet Surface\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"vehicleOptions\":[\n" +
            "    {\n" +
            "      \"id\":\"eRallyGrpACaps\",\n" +
            "      \"name\":\"Group A\",\n" +
            "      \"dlcStatus\":\"ContainsDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyGrpB4wdCaps\",\n" +
            "      \"name\":\"Group B (4WD)\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyGrpBRwdCaps\",\n" +
            "      \"name\":\"Group B (RWD)\",\n" +
            "      \"dlcStatus\":\"IsSolelyDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyKitcarCaps\",\n" +
            "      \"name\":\"F2 Kit Car\",\n" +
            "      \"dlcStatus\":\"IsSolelyDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyR5Caps\",\n" +
            "      \"name\":\"R5\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyUpTo20004wdCaps\",\n" +
            "      \"name\":\"Up to 2000cc (4WD)\",\n" +
            "      \"dlcStatus\":\"IsSolelyDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyNr4R4Caps\",\n" +
            "      \"name\":\"NR4/R4\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyH2RwdCaps\",\n" +
            "      \"name\":\"H2 (RWD)\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyH3RwdCaps\",\n" +
            "      \"name\":\"H3 (RWD)\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyR2Caps\",\n" +
            "      \"name\":\"R2\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyH2FwdCaps\",\n" +
            "      \"name\":\"H2 (FWD)\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyH1FwdCaps\",\n" +
            "      \"name\":\"H1 (FWD)\",\n" +
            "      \"dlcStatus\":\"ContainsNoDlcVehicles\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":\"eRallyRGtCaps\",\n" +
            "      \"name\":\"Rally GT\",\n" +
            "      \"dlcStatus\":\"ContainsDlcVehicles\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";


}