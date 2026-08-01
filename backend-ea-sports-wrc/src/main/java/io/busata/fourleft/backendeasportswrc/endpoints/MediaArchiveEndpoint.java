package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.CreateMediaArchivePostTo;
import io.busata.fourleft.api.easportswrc.models.MediaArchiveFeedTo;
import io.busata.fourleft.backendeasportswrc.domain.services.mediaarchive.MediaArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MediaArchiveEndpoint {

    private final MediaArchiveService mediaArchiveService;

    @PostMapping("/api_v2/media-archive/posts")
    public void ingestPost(@RequestBody CreateMediaArchivePostTo post) {
        mediaArchiveService.ingest(post);
    }

    @GetMapping("/api_v2/media-archive/channels/{channelId}/message-ids")
    public List<Long> getArchivedMessageIds(@PathVariable long channelId) {
        return mediaArchiveService.getArchivedMessageIds(channelId);
    }

    @GetMapping("/api_v2/media-archive/feed")
    public MediaArchiveFeedTo getFeed(@RequestParam(required = false) String before,
                                      @RequestParam(defaultValue = "20") int size) {
        Long cursor = before == null || before.isBlank() ? null : Long.parseLong(before);
        return mediaArchiveService.getFeed(cursor, size);
    }
}
