package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.TimeTrialCombination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeTrialCombinationRepository extends JpaRepository<TimeTrialCombination, String> {

    /** Every combination for one rally — the scope of a single probe job. */
    List<TimeTrialCombination> findByLocationId(Long locationId);
}
