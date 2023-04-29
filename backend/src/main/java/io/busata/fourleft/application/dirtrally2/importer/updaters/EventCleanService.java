package io.busata.fourleft.application.dirtrally2.importer.updaters;

import io.busata.fourleft.domain.dirtrally2.clubs.models.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.repository.ClubRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventCleanService {

    private final EventRepository eventRepository;

    private final ClubRepository clubRepository;

    @Transactional
    public void cleanArchived() {
       List<Championship> championships = eventRepository.findByArchived(true).stream().map(Event::getChampionship).toList();;

       championships.forEach(championship -> {
           Club club = championship.getClub();
           club.removeChampionship(championship);
           clubRepository.save(club);
       });
    }

}
