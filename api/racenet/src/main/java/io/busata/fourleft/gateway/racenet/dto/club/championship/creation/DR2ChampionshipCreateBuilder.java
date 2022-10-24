package io.busata.fourleft.gateway.racenet.dto.club.championship.creation;


import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class DR2ChampionshipCreateBuilder {
    @Setter
    boolean allowAssists = true;


    @Setter
    boolean forceCockpitCamera = false;
    long restartsLimits = 0;

    @Setter
    String start = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1).toString();

    @Setter
    boolean useHardcoreDamage = false;

    @Setter
    boolean useUnexpectedMoments = false;

    public DR2ChampionshipCreateBuilder withEvents(DR2ChampionshipCreateEventBuilder... events) {
        this.events = Arrays.stream(events).map(DR2ChampionshipCreateEventBuilder::build).collect(Collectors.toList());
        return this;
    }

    List<DR2ChampionShipCreateEvent> events = new ArrayList<>();


    public static DR2ChampionshipCreateBuilder championship() {
        return new DR2ChampionshipCreateBuilder();
    }

    public DR2ChampionshipCreateRequestTo build() {

        if(events.size() == 0) {
            throw new RuntimeException("Need at least one event to create a championship");
        }

        return new DR2ChampionshipCreateRequestTo(
                allowAssists,
                forceCockpitCamera,
                restartsLimits,
                start,
                useHardcoreDamage,
                useUnexpectedMoments,
                events
        );
    }
}
