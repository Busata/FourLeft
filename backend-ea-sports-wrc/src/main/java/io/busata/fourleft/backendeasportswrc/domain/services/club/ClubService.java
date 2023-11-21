package io.busata.fourleft.backendeasportswrc.domain.services.club;

import io.busata.fourleft.backendeasportswrc.application.importer.ClubFactory;
import io.busata.fourleft.backendeasportswrc.application.importer.results.StandingsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.models.*;
import io.busata.fourleft.backendeasportswrc.domain.services.championships.ChampionshipService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ChampionshipService championshipService;
    private final ClubFactory clubFactory;


    @Transactional(readOnly = true)
    public boolean exists(String id) {
        return this.clubRepository.existsById(id);
    }


    @Transactional
    public void createClub(ClubDetailsTo clubDetailsTo, List<ChampionshipTo> championships) {
        Club club = this.clubFactory.create(clubDetailsTo, championships);

        this.clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public boolean requiresDetailUpdate(String clubId) {
        Club club = this.findById(clubId);
        return club.requiresDetailsUpdate() || this.hasActiveEventThatFinished(clubId) || this.hasUpcomingChampionshipThatStarted(clubId);
    }

    @Transactional(readOnly = true)
    public boolean requiresLeaderboardUpdate(String clubId) {
        return !getLeaderboardsRequiringUpdate(clubId).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean requiresHistoryUpdate(String clubId) {
        Club club = this.findById(clubId);
        return club.getChampionships().stream()
                .filter(c -> c.getStatus() == EventStatus.FINISHED)
                .anyMatch(c -> !c.isUpdatedAfterFinish())
                ||
                club.getChampionships().stream()
                        .filter(c -> c.getStatus() == EventStatus.OPEN)
                        .filter(c -> c.getEvents().stream().anyMatch(event -> event.getStatus() == EventStatus.FINISHED))
                        .anyMatch(c -> c.getStandings().isEmpty());
    }

    @Transactional(readOnly = true)
    public List<String> getLeaderboardsRequiringUpdate(String clubId) {
        Club club = this.findById(clubId);
        return club.getLeadersboardsRequiringUpdate();
    }

    @Transactional(readOnly = true)
    public List<String> getOpenLeaderboards(String clubId) {
        Club club = this.findById(clubId);
        return club.getOpenLeaderboards();
    }

    @Transactional(readOnly = true)
    public Set<String> getHistoryLeaderboards(String clubId) {
        Club club = this.findById(clubId);
        return club.getHistoryLeaderboards();
    }


    @Transactional(readOnly = true)
    public Set<String> getHistoryChampionships(String clubId) {
        Club club = this.findById(clubId);

        return Stream.concat(
                club.getChampionships().stream().filter(c -> c.getStatus() == EventStatus.FINISHED).filter(c -> !c.isUpdatedAfterFinish()).map(Championship::getId),
                club.getChampionships().stream().filter(c -> c.getStatus() == EventStatus.OPEN).filter(c -> c.getStandings().isEmpty()).map(Championship::getId)).collect(Collectors.toSet()
        );
    }


    @Transactional(readOnly = true)
    public Optional<String> getActiveChampionshipId(String clubId) {
        Club club = this.findById(clubId);
        return club.getActiveChampionshipSnapshot().map(Championship::getId);
    }



    @Transactional(readOnly = true)
    public Optional<String> getUpcomingChampionshipId(String clubId) {
        Club club = this.findById(clubId);
        return club.getUpcomingChampionshipSnapshot().map(Championship::getId);
    }




    @Transactional(readOnly = true)
    public Optional<Event> getActiveEvent(String clubId) {
        Club club = this.findById(clubId);
        return club.getActiveChampionshipSnapshot().flatMap(Championship::getActiveEventSnapshot);
    }

    @Transactional
    public void updateClub(ClubDetailsTo clubDetails, List<ChampionshipTo> championships) {
        Club existingClub = this.findById(clubDetails.clubID());

        Club updatedClub = this.clubFactory.update(existingClub, clubDetails, championships);

        updatedClub.markUpdated();

        this.clubRepository.save(updatedClub);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEventThatFinished(String clubId) {
        Club club = this.clubRepository.findById(clubId).orElseThrow();
        return club.getActiveChampionshipSnapshot().flatMap(Championship::getActiveEventSnapshot).map(Event::isActiveNow).map(x -> !x).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasUpcomingChampionshipThatStarted(String clubId) {
        Club club = this.clubRepository.findById(clubId).orElseThrow();
        return club.getUpcomingChampionshipSnapshot().map(Championship::isActiveNow).orElse(false);
    }

    @Transactional(readOnly = true)
    public Club findById(String id) {
        return this.clubRepository.findById(id).orElseThrow();
    }


    @Transactional
    public void markBoardAsUpdated(String leaderboardId) {
        this.clubRepository.markBoardAsUpdated(leaderboardId, ApplicationClock.now());
    }

    @Transactional
    public void updateStandings(StandingsUpdatedResult standingsUpdatedResult) {
        Club club = findById(standingsUpdatedResult.getClubId());

        club.getChampionships().stream().filter(championship -> {
            return championship.getId().equals(standingsUpdatedResult.getChampionshipId());
        }).findFirst().ifPresent(championship -> {
            championship.update(standingsUpdatedResult.getEntries().stream().map(clubFactory::createStanding).map(result -> {
                result.setChampionship(championship);
                return result;
            }).collect(Collectors.toMap(ChampionshipStanding::getRank, e -> e, (e, v) -> e)).values().stream().toList());
            championshipService.save(championship);
        });

        this.clubRepository.save(club);
    }

    @Transactional
    public void markHistoryUpdateDone(String clubId) {
        Club club = this.findById(clubId);
        club.getChampionships().stream().filter(c -> c.getStatus() == EventStatus.FINISHED).filter(c -> !c.isUpdatedAfterFinish()).forEach(championship -> championship.setUpdatedAfterFinish(true));
        clubRepository.save(club);
    }

    @Transactional
    public Optional<Event> findPreviousEvent(String clubId) {
        Club club = this.findById(clubId);

        return club.getActiveChampionshipSnapshot().flatMap(this::getPreviousEvent).or(() -> {
            return getPreviousChampionship(club).flatMap(this::getLastEvent);
        });
    }

    private Optional<Event> getLastEvent(Championship championship) {
        List<Event> events = championship.getEvents().stream().sorted(Comparator.comparing(Event::getAbsoluteCloseDate)).toList();

        if (events.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(events.get(events.size() - 1));
        }

    }

    public Optional<Event> getPreviousEvent(Championship championship) {
        List<Event> events = championship.getEvents().stream().sorted(Comparator.comparing(Event::getAbsoluteCloseDate)).toList();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);

            if (event.getStatus() == EventStatus.OPEN) {
                if (i > 0) {
                    return Optional.of(events.get(i - 1));
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();

    }

    public Optional<Championship> getPreviousChampionship(Club club) {
        List<Championship> championships = club.getChampionships().stream().sorted(Comparator.comparing(Championship::getAbsoluteCloseDate)).toList();
        for (int i = 0; i < championships.size(); i++) {
            Championship championship = championships.get(i);

            if (championship.getStatus() == EventStatus.OPEN) {
                if (i > 0) {
                    return Optional.of(championships.get(i - 1));
                } else {
                    return Optional.empty();
                }
            }
        }

        if(championships.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(championships.get(championships.size() - 1));
        }
    }
}
