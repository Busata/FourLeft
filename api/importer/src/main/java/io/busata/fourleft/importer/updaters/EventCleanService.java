package io.busata.fourleft.importer.updaters;

import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.repository.ClubRepository;
import io.busata.fourleft.domain.clubs.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventCleanService {

    private final EventRepository eventRepository;

    private final ClubRepository clubRepository;

    public void cleanArchived() {
       List<Championship> championships = eventRepository.findByArchived(true).stream().map(Event::getChampionship).toList();;

       championships.forEach(championship -> {
           Club club = championship.getClub();
           club.removeChampionship(championship);
           clubRepository.save(club);
       });
    }

}
