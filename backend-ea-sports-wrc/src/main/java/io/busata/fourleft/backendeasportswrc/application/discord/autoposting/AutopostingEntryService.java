package io.busata.fourleft.backendeasportswrc.application.discord.autoposting;

import io.busata.fourleft.backendeasportswrc.domain.models.autoposting.AutopostEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutopostingEntryService {
    private final AutopostEntryRepository autopostEntryRepository;


    List<AutopostEntry> findPostedEntries(String eventId, Long channelId) {
        return autopostEntryRepository.findPostedEntries(eventId, channelId);
    }

    public void saveEntries(List<AutopostEntry> collect) {
        this.autopostEntryRepository.saveAll(collect);
    }
}
