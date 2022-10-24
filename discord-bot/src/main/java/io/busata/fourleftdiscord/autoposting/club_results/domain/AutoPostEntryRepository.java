package io.busata.fourleftdiscord.autoposting.club_results.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutoPostEntryRepository extends JpaRepository<AutoPostEntry, UUID> {

    List<AutoPostEntry> findByEventIdAndChallengeId(String eventId, String challengeId);
    List<AutoPostEntry> findByMessageId(Long messageId);
    long countByMessageId(Long messageId);

}