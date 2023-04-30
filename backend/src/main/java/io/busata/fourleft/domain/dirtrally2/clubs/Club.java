package io.busata.fourleft.domain.dirtrally2.clubs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "club")
@Getter
@Setter
@NoArgsConstructor
public class Club {
    @Id
    @GeneratedValue
    private UUID id;

    private Long referenceId;

    private String name;
    private String description;
    private long members;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("referenceId asc")
    private List<Championship> championships = new ArrayList<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubMember> clubMembers = new ArrayList<>();

    private LocalDateTime lastUpdate = LocalDateTime.now();

    public Club(Long referenceId) {
        this.referenceId = referenceId;
    }

    public void updateChampionships(List<Championship> value) {
        championships.clear();
        championships.addAll(value);
    }

    public void updateMembers(List<ClubMember> members) {
        clubMembers.clear();
        clubMembers.addAll(members);
    }

    public boolean requiresRefresh() {
        if(lastUpdate == null) {
            return true;
        }

        return Duration.between(lastUpdate, LocalDateTime.now()).toMinutes() >= 60;
    }

    public void markRefreshed() {
        this.lastUpdate = LocalDateTime.now();
    }

    public Optional<Event> getCurrentEvent() {
        return findActiveChampionship()
                .flatMap(championship -> championship.getEvents().stream().filter(Event::isCurrent).findFirst());
    }

    public Optional<Event> getPreviousEvent() {
        return findActiveChampionship()
                .flatMap(championship ->
                        championship.getEvents().stream()
                                .sorted(Comparator.comparing(Event::getOrder))
                                .filter(Event::isPrevious)
                                .reduce((first, second) -> second)
                )
                .or(() -> findPreviousChampionship().flatMap(championship ->
                        championship.getEvents().stream()
                                .sorted(Comparator.comparing(Event::getOrder))
                                .reduce((first, second) -> second))
                );
    }

    public Optional<Championship> findActiveChampionship() {
        return getChampionships().stream()
                .filter(Championship::isActive).findFirst();
    }

    public Optional<Championship> findPreviousChampionship() {
        return getChampionships().stream()
                .sorted(Comparator.comparing(Championship::getOrder).reversed())
                .filter(Championship::isInActive).findFirst();
    }


    public void merge(List<Championship> championships) {
        var replacedWithUpdated = getChampionships().stream()
                .map(current -> championships.stream().filter(updated -> updated.equals(current)).findFirst().orElse(current))
                .collect(Collectors.toList());

        for (Championship updated : championships) {
            if (!replacedWithUpdated.contains(updated)) {
                replacedWithUpdated.add(updated);
            }
        }

        updateChampionships(replacedWithUpdated);

    }


    public void removeChampionship(Championship championship) {
        final var updated = getChampionships().stream().filter(current -> !current.getId().equals(championship.getId())).collect(Collectors.toList());

        updateChampionships(updated);
    }
}
