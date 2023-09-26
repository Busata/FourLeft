package io.busata.fourleft.application.dirtrally2.importer;

import io.busata.fourleft.domain.dirtrally2.clubs.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.ClubRepository;
import io.busata.fourleft.domain.dirtrally2.clubs.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
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
