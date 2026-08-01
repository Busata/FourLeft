package io.busata.fourleft.backendeasportswrc.domain.services.mediaarchive;

import io.busata.fourleft.backendeasportswrc.domain.models.MediaArchiveEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MediaArchiveEntryRepository extends JpaRepository<MediaArchiveEntry, UUID> {

    boolean existsByAttachmentId(long attachmentId);

    @Query("select distinct e.messageId from MediaArchiveEntry e where e.channelId = :channelId")
    List<Long> findMessageIdsByChannelId(@Param("channelId") long channelId);

    @Query("select distinct e.messageId from MediaArchiveEntry e order by e.messageId desc")
    List<Long> findLatestMessageIds(Pageable pageable);

    @Query("select distinct e.messageId from MediaArchiveEntry e where e.messageId < :before order by e.messageId desc")
    List<Long> findMessageIdsBefore(@Param("before") long before, Pageable pageable);

    List<MediaArchiveEntry> findByMessageIdInOrderByAttachmentIdAsc(Collection<Long> messageIds);
}
