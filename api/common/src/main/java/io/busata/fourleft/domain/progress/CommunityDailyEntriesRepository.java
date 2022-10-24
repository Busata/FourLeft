package io.busata.fourleft.domain.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommunityDailyEntriesRepository extends JpaRepository<CommunityDailyEntries, UUID> {


    List<CommunityDailyEntries> findByName(String name);
}