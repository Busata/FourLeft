package io.busata.fourleft.backendeasportswrc.domain.models;

/**
 * The kinds of work the queue can run. One value per {@code JobHandler}. Scoped to CLUB for now;
 * add a value + a handler (+ optionally a target) to introduce another job type.
 */
public enum JobType {
    CLUB
}
