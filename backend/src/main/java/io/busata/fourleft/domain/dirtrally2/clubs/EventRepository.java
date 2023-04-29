package io.busata.fourleft.domain.dirtrally2.clubs;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByArchived(boolean archived);
}
