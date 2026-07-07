package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.ClubTo;
import io.busata.fourleft.api.acrally.models.CreateClubRequestTo;
import io.busata.fourleft.backendacrally.domain.models.club.Club;
import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.club.ClubMembershipRepository;
import io.busata.fourleft.backendacrally.domain.services.club.ClubMembershipRepository.ClubMemberCount;
import io.busata.fourleft.backendacrally.domain.services.club.ClubMembershipService;
import io.busata.fourleft.backendacrally.domain.services.club.ClubRepository;
import io.busata.fourleft.backendacrally.domain.services.club.ClubService;
import io.busata.fourleft.backendacrally.domain.services.user.AppUserRepository;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Community clubs — the ACRally dashboard list, plus join/leave. Session (cookie) authenticated. */
@RestController
@RequestMapping("/acrally-api/clubs")
@RequiredArgsConstructor
public class ClubEndpoint {

    private final ClubService clubService;
    private final ClubMembershipService membershipService;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping
    public List<ClubTo> list(@AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        return toTos(clubRepository.findAllByOrderByCreatedAtDesc(), principal.getId());
    }

    /** The clubs the signed-in user has joined, most recently joined first. */
    @GetMapping("/mine")
    public List<ClubTo> mine(@AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        Set<UUID> myClubIds = membershipRepository.findClubIdsByUserId(principal.getId());
        List<Club> clubs = clubRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(club -> myClubIds.contains(club.getId()))
                .toList();
        return toTos(clubs, principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClubTo create(@RequestBody CreateClubRequestTo request, @AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        Club club = clubService.create(principal.getId(), request.name(), request.description(), request.socialLink());
        return toTos(List.of(club), principal.getId()).get(0);
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void join(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        membershipService.join(id, principal.getId());
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        membershipService.leave(id, principal.getId());
    }

    /** Builds TOs for a set of clubs, enriched with member counts, creator names and the viewer's membership. */
    private List<ClubTo> toTos(List<Club> clubs, UUID viewerId) {
        if (clubs.isEmpty()) {
            return List.of();
        }
        Map<UUID, Long> counts = membershipRepository.countPerClub().stream()
                .collect(Collectors.toMap(ClubMemberCount::getClubId, ClubMemberCount::getMemberCount));
        Set<UUID> memberOf = membershipRepository.findClubIdsByUserId(viewerId);
        Map<UUID, String> creatorNames = appUserRepository.findAllById(
                        clubs.stream().map(Club::getCreatedBy).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(AppUser::getId, AppUser::getDisplayName));

        return clubs.stream()
                .map(club -> new ClubTo(
                        club.getId(),
                        club.getName(),
                        club.getDescription(),
                        club.getSocialLink(),
                        creatorNames.get(club.getCreatedBy()),
                        counts.getOrDefault(club.getId(), 0L),
                        memberOf.contains(club.getId()),
                        club.getCreatedBy().equals(viewerId),
                        club.getCreatedAt()))
                .toList();
    }

    private void requireLogin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
