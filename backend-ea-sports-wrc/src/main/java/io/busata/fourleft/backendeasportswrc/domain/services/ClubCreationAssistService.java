package io.busata.fourleft.backendeasportswrc.domain.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.busata.fourleft.api.easportswrc.models.ClubCreationAssistSummary;
import io.busata.fourleft.backendeasportswrc.domain.models.Championship;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;
import io.busata.fourleft.backendeasportswrc.domain.models.EventSettings;
import io.busata.fourleft.backendeasportswrc.domain.models.EventStatus;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubCreationAssistService {

    private final ClubService clubService;

    @Transactional(readOnly = true)
    public ClubCreationAssistSummary createSummary(String clubId) {
        final var club = clubService.findById(clubId);
        var events = club.getChampionships().stream()
        .filter(championship -> championship.getStatus() == EventStatus.FINISHED || championship.getStatus() == EventStatus.OPEN)
        .flatMap(championship -> championship.getEvents().stream())
        .sorted(Comparator.comparing(Event::getAbsoluteCloseDate).reversed())
        .toList();


        
        final Map<String, Long> vehicleClassCounts = events.stream()
        .map(Event::getEventSettings)
        .map(EventSettings::getVehicleClass)
        .collect(Collectors.toMap(e -> e, e -> 0L, Long::sum));



        events.stream()
        .map(Event::getEventSettings)
        .map(EventSettings::getVehicleClass)
        .limit(21)
        .forEach(vehicleClass -> {
            vehicleClassCounts.computeIfPresent(vehicleClass, (k, v) -> v + 1L);
        });

        
        final Map<String, Long> locationCounts = events.stream()
        .map(Event::getEventSettings)
        .map(EventSettings::getLocation)
        .collect(Collectors.toMap(e -> e, e -> 0L, Long::sum));

        
        events.stream()
        .map(Event::getEventSettings)
        .map(EventSettings::getLocation)
        .limit(21)
        .forEach(locationId -> {
            locationCounts.computeIfPresent(locationId, (k, v) -> v + 1L);
        });



        List<String> vehicles = vehicleClassCounts.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .limit(7)
        .collect(Collectors.toList());

        List<String> locations = locationCounts.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .limit(7)
        .collect(Collectors.toList());

        return new ClubCreationAssistSummary(vehicles, locations);


    }

}
