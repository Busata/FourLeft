package io.busata.fourleft.backendeasportswrc.application.importer;

public class ClubImportFailedException extends RuntimeException {
    public ClubImportFailedException(String clubId) {
        super("Club import failed for club " + clubId);
    }
}
