package io.busata.fourleft.backendeasportswrc.domain.services.timetrial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Per-class car catalog from the time-trial boards, loaded once at startup. The catalog doesn't change
 * anymore, and deriving it per request scans ~200k entries per class (~3s cold on prod). The full load
 * takes tens of seconds, so it runs off the startup thread; until it lands, lookups query directly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeTrialVehicleCatalog {
    private final TimeTrialLeaderboardEntryRepository repository;

    private volatile Map<Long, List<String>> vehiclesByClass;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        Thread.ofVirtual().name("tt-vehicle-catalog").start(() -> {
            try {
                long start = System.currentTimeMillis();
                vehiclesByClass = repository.findDistinctVehiclesPerClass().stream()
                        .collect(Collectors.groupingBy(row -> (Long) row[0],
                                Collectors.mapping(row -> (String) row[1],
                                        Collectors.collectingAndThen(Collectors.toCollection(TreeSet::new), List::copyOf))));
                log.info("Loaded time-trial vehicle catalog: {} classes in {} ms", vehiclesByClass.size(), System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("Failed to load time-trial vehicle catalog, falling back to per-request queries", e);
            }
        });
    }

    /** Distinct cars driven in the given classes, sorted. */
    public List<String> findVehicles(Set<Long> classIds) {
        Map<Long, List<String>> catalog = vehiclesByClass;
        if (catalog == null) {
            return repository.findDistinctVehiclesByClassIds(classIds);
        }
        return classIds.stream()
                .flatMap(classId -> catalog.getOrDefault(classId, List.of()).stream())
                .distinct()
                .sorted()
                .toList();
    }
}
