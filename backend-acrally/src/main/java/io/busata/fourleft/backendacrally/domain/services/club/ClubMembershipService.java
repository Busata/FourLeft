package io.busata.fourleft.backendacrally.domain.services.club;

import io.busata.fourleft.backendacrally.domain.models.club.ClubMembership;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubMembershipService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;

    /** Idempotent: joining a club you're already in is a no-op. */
    @Transactional
    public void join(UUID clubId, UUID userId) {
        if (!clubRepository.existsById(clubId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such club.");
        }
        if (!membershipRepository.existsByClubIdAndUserId(clubId, userId)) {
            membershipRepository.save(new ClubMembership(clubId, userId));
        }
    }

    /**
     * Idempotent: leaving a club you're not in is a no-op. The club's creator cannot
     * leave their own club — they're the owner and stay a member.
     */
    @Transactional
    public void leave(UUID clubId, UUID userId) {
        clubRepository.findById(clubId).ifPresent(club -> {
            if (club.getCreatedBy().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The club creator cannot leave their own club.");
            }
        });
        membershipRepository.deleteByClubIdAndUserId(clubId, userId);
    }
}
