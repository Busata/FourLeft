package io.busata.fourleft.backendeasportswrc.application.importer.process.core;

import java.util.Map;
import java.util.function.Consumer;

public interface ClubImportProcessHandler {

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClubImportProcessHandler.class);

    Map<ProcessState, Consumer<ClubImportProcess>> getStrategies();

    default boolean canHandle(ClubImportProcess process) {
        return getStrategies().containsKey(process.getState());
    }

    default void control(ClubImportProcess process) {
        try {
            getStrategies().get(process.getState()).accept(process);
        } catch (Exception ex) {
            log.error("Something went wrong during processing the club", ex);
            process.markFailed();
        }
    }
}
