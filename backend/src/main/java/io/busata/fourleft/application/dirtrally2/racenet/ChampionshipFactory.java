package io.busata.fourleft.application.dirtrally2.racenet;

import io.busata.fourleft.domain.dirtrally2.clubs.models.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Event;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubChampionships;
import io.busata.fourleft.infrastructure.clients.racenet.dto.club.DR2ClubResultChampionship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChampionshipFactory {
    private final EventFactory eventFactory;

    public Championship create(DR2ClubResultChampionship clubResultChampionship) {
        Championship championship = new Championship();
        championship.setName(clubResultChampionship.name());
        championship.setReferenceId(clubResultChampionship.id());
        List<Event> events = clubResultChampionship.events().stream().map(eventFactory::create).peek(event -> event.setChampionship(championship)).collect(Collectors.toList());

        championship.updateEvents(events);

        return championship;
    }

    public Championship enrich(Championship championship, DR2ClubChampionships details) {
        championship.setHardcoreDamage(details.useHardcoreDamage());
        championship.setForceCockpitCamera(details.forceCockpitCamera());
        championship.setActive(details.isActive());
        championship.setAllowAssists(details.allowAssists());

        championship.updateEvents(eventFactory.enrichEvents(details, championship.getEvents()));

        return championship;
    }
}
