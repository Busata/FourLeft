package io.busata.fourleft.backendacrally.domain.services.club;

import io.busata.fourleft.backendacrally.domain.models.club.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, UUID> {

    boolean existsByClubIdAndUserId(UUID clubId, UUID userId);

    void deleteByClubIdAndUserId(UUID clubId, UUID userId);

    /** The set of clubs the given user has joined — used to flag membership on the club list. */
    @Query("select m.clubId from ClubMembership m where m.userId = :userId")
    Set<UUID> findClubIdsByUserId(UUID userId);

    /** Member counts grouped per club, for the whole set of clubs. */
    @Query("select m.clubId as clubId, count(m) as memberCount from ClubMembership m group by m.clubId")
    List<ClubMemberCount> countPerClub();

    interface ClubMemberCount {
        UUID getClubId();

        long getMemberCount();
    }
}
