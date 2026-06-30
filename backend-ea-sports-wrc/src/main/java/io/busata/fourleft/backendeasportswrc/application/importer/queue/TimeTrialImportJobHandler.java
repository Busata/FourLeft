package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * STUB. Time-trial imports are greenfield (no TT entity/import exists yet).
 * <p>
 * When implemented, this should fetch the TT leaderboard for the combo encoded in
 * {@code job.getRef()} (e.g. "track:car:surface"), persist it, and return
 * {@link JobResult#dataChanged()} / {@link JobResult#noChange()} depending on whether
 * the data actually moved. Returning the accurate flag is what lets the adaptive
 * cadence keep popular combos hot and let dead ones drift toward their ceiling,
 * so the "too many combinations" firehose costs almost nothing for stale entries.
 */
@Component
@Slf4j
public class TimeTrialImportJobHandler implements JobHandler {

    @Override
    public ImportType type() {
        return ImportType.TT;
    }

    @Override
    public JobResult handle(ImportJob job) {
        log.warn("TT import not implemented yet - skipping ref {}", job.getRef());
        // Report 'unchanged' so a (future) adaptive TT target backs off instead of
        // hammering an unimplemented path.
        return JobResult.noChange();
    }
}
