package io.busata.fourleft.schedules;


import io.busata.fourleft.domain.clubs.repository.UniquePlayersViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="query", havingValue="true", matchIfMissing = true)
public class QueryUpdateSchedule {

    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(cron = "0 */1 * * * *", zone = "UTC")
    public void refreshUniquePlayersView() {
        Query nativeQuery = entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW unique_players;");
        nativeQuery.executeUpdate();

    }
}
