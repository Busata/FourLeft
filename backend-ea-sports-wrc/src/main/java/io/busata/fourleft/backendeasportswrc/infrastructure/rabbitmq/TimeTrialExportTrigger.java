package io.busata.fourleft.backendeasportswrc.infrastructure.rabbitmq;

import io.busata.fourleft.api.easportswrc.EASportsWRCQueueNames;
import io.busata.fourleft.api.easportswrc.events.TimeTrialBoardFetchedEvent;
import io.busata.fourleft.backendeasportswrc.application.work.queue.JobService;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Turns a board-fetched broadcast into a CSV re-export: every {@code TT_BOARD_FETCHED} message
 * enqueues a {@code TT_EXPORT} job for that board (deduped against one already queued/running).
 * The export runs in the work queue, not on the listener thread — a bulk fetch sweep fanning out
 * thousands of these stays bounded by the queue's per-type concurrency budget.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TimeTrialExportTrigger {

    private final JobService jobService;

    @RabbitListener(queues = EASportsWRCQueueNames.EA_SPORTS_WRC_TT_BOARD_FETCHED)
    public void onBoardFetched(TimeTrialBoardFetchedEvent event) {
        jobService.enqueueIfAbsent(JobType.TT_EXPORT, event.combinationId())
                .ifPresent(job -> log.debug("Enqueued CSV export for board {} (job {})",
                        event.combinationId(), job.getId()));
    }
}
