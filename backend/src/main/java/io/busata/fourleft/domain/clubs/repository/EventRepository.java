package io.busata.fourleft.domain.clubs.repository;

import io.busata.fourleft.domain.clubs.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByArchived(boolean archived);
}
