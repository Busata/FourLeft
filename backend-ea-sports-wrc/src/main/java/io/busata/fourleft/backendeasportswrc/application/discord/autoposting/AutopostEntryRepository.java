package io.busata.fourleft.backendeasportswrc.application.discord.autoposting;

import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutoPostEntryId;
import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutopostEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface AutopostEntryRepository extends JpaRepository<AutopostEntry, AutoPostEntryId> {

    @Query("select a from AutopostEntry a where a.eventId=:eventId and a.channelId=:channelId")
    List<AutopostEntry> findPostedEntries(@Param("eventId") String eventId, @Param("channelId") Long channelId);
}